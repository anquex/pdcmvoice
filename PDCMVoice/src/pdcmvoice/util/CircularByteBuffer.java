/*
 * Circular Byte Buffer
 * Copyright (C) 2002 Stephen Ostermiller
 * http://ostermiller.org/contact.pl?regarding=Java+Utilities
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * See COPYING.TXT for details.
 */
package pdcmvoice.util;

import java.io.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implements the Circular Buffer producer/consumer model for bytes.
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/CircularByteBuffer.html">ostermiller.org</a>.
 * <p>
 * Using this class is a simpler alternative to using a PipedInputStream
 * and a PipedOutputStream. PipedInputStreams and PipedOutputStreams don't support the
 * mark operation, don't allow you to control buffer sizes that they use,
 * and have a more complicated API that requires instantiating two
 * classes and connecting them.
 * <p>
 * This class is thread safe.
 *
 * @see CircularCharBuffer
 * @see CircularObjectBuffer
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.00.00
 */
public class CircularByteBuffer {
        
        private final boolean DEBUG=false;
        
        private final ReentrantLock lock=new ReentrantLock(true);
        private final Condition notEnoughtFreeSpace=lock.newCondition();
        private final Condition notEnoughtDataToRead=lock.newCondition();
        
	/**
	 * The default size for a circular byte buffer.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private final static int DEFAULT_SIZE = 1024;

	/**
	 * A buffer that will grow as things are added.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public final static int INFINITE_SIZE = -1;

	/**
	 * The circular buffer.
	 * <p>
	 * The actual capacity of the buffer is one less than the actual length
	 * of the buffer so that an empty and a full buffer can be
	 * distinguished.  An empty buffer will have the markPostion and the
	 * writePosition equal to each other.  A full buffer will have
	 * the writePosition one less than the markPostion.
	 * <p>
	 * There are three important indexes into the buffer:
	 * The readPosition, the writePosition, and the markPosition.
	 * If the InputStream has never been marked, the readPosition and
	 * the markPosition should always be the same.  The bytes
	 * available to be read go from the readPosition to the writePosition,
	 * wrapping around the end of the buffer.  The space available for writing
	 * goes from the write position to one less than the markPosition,
	 * wrapping around the end of the buffer.  The bytes that have
	 * been saved to support a reset() of the InputStream go from markPosition
	 * to readPosition, wrapping around the end of the buffer.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected byte[] buffer;
	/**
	 * Index of the first byte available to be read.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected volatile int readPosition = 0;
	/**
	 * Index of the first byte available to be written.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected volatile int writePosition = 0;
	/**
	 * Index of the first saved byte. (To support stream marking.)
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected volatile int markPosition = 0;
	/**
	 * Number of bytes that have to be saved
	 * to support mark() and reset() on the InputStream.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected volatile int markSize = 0;
	/**
	 * If this buffer is infinite (should resize itself when full)
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected volatile boolean infinite = false;
	/**
	 * True if a write to a full buffer should block until the buffer
	 * has room, false if the write method should throw an IOException
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected boolean blockingWrite = true;
	/**
	 * The InputStream that can empty this buffer.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected InputStream in = new CircularByteBufferInputStream();
	/**
	 * true if the close() method has been called on the InputStream
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected boolean inputStreamClosed = false;
	/**
	 * The OutputStream that can fill this buffer.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected OutputStream out = new CircularByteBufferOutputStream();
	/**
	 * true if the close() method has been called on the OutputStream
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected boolean outputStreamClosed = false;

	/**
	 * Make this buffer ready for reuse.  The contents of the buffer
	 * will be cleared and the streams associated with this buffer
	 * will be reopened if they had been closed.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void clear(){
                lock.lock();
		try{
			readPosition = 0;
			writePosition = 0;
			markPosition = 0;
			outputStreamClosed = false;
			inputStreamClosed = false;
		}finally{lock.unlock();}
	}

	/**
	 * Retrieve a OutputStream that can be used to fill
	 * this buffer.
	 * <p>
	 * Write methods may throw a BufferOverflowException if
	 * the buffer is not large enough.  A large enough buffer
	 * size must be chosen so that this does not happen or
	 * the caller must be prepared to catch the exception and
	 * try again once part of the buffer has been consumed.
	 *
	 *
	 * @return the producer for this buffer.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public OutputStream getOutputStream(){
		return out;
	}

	/**
	 * Retrieve a InputStream that can be used to empty
	 * this buffer.
	 * <p>
	 * This InputStream supports marks at the expense
	 * of the buffer size.
	 *
	 * @return the consumer for this buffer.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public InputStream getInputStream(){
		return in;
	}

	/**
	 * Get number of bytes that are available to be read.
	 * <p>
	 * Note that the number of bytes available plus
	 * the number of bytes free may not add up to the
	 * capacity of this buffer, as the buffer may reserve some
	 * space for other purposes.
	 *
	 * @return the size in bytes of this buffer
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public int getAvailable(){
		lock.lock();
		try{
			return available();
		}finally{lock.unlock();}
	}

	/**
	 * Get the number of bytes this buffer has free for
	 * writing.
	 * <p>
	 * Note that the number of bytes available plus
	 * the number of bytes free may not add up to the
	 * capacity of this buffer, as the buffer may reserve some
	 * space for other purposes.
	 *
	 * @return the available space in bytes of this buffer
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public int getSpaceLeft(){
		lock.lock();
		try{
			return spaceLeft();
		}finally{lock.unlock();}
	}

	/**
	 * Get the capacity of this buffer.
	 * <p>
	 * Note that the number of bytes available plus
	 * the number of bytes free may not add up to the
	 * capacity of this buffer, as the buffer may reserve some
	 * space for other purposes.
	 *
	 * @return the size in bytes of this buffer
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public int getSize(){
		lock.lock();
		try{
			return buffer.length;
		}finally{lock.unlock();}
	}

	/**
	 * double the size of the buffer
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private void resize(){
		byte[] newBuffer = new byte[buffer.length * 2];
		int marked = marked();
		int available = available();
		if (markPosition <= writePosition){
			// any space between the mark and
			// the first write needs to be saved.
			// In this case it is all in one piece.
			int length = writePosition - markPosition;
			System.arraycopy(buffer, markPosition, newBuffer, 0, length);
		} else {
			int length1 = buffer.length - markPosition;
			System.arraycopy(buffer, markPosition, newBuffer, 0, length1);
			int length2 = writePosition;
			System.arraycopy(buffer, 0, newBuffer, length1, length2);
		}
		buffer = newBuffer;
		markPosition = 0;
		readPosition = marked;
		writePosition = marked + available;
	}

	/**
	 * Space available in the buffer which can be written.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private int spaceLeft(){
		if (writePosition < readPosition){
			// any space between the first write and
			// the mark except one byte is available.
			// In this case it is all in one piece.
			return (readPosition - writePosition - 1);
		}
		// space at the beginning and end.
		return ((buffer.length - 1) - (writePosition - readPosition));
	}

	/**
	 * Bytes available for reading.
	 *
	 * @since ostermillerutils 1.00.00
	 */
        
        //this is correct (use all cells)
	private int available(){
		if (readPosition <= writePosition){
			// any space between the first read and
			// the first write is available.  In this case i
			// is all in one piece.
			return (writePosition - readPosition);
		}
		// space at the beginning and end.
		return (buffer.length - (readPosition - writePosition));
	}

	/**
	 * Bytes saved for supporting marks.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private int marked(){
		if (markPosition <= readPosition){
			// any space between the markPosition and
			// the first write is marked.  In this case i
			// is all in one piece.
			return (readPosition - markPosition);
		}
		// space at the beginning and end.
		return (buffer.length - (markPosition - readPosition));
	}

	/**
	 * If we have passed the markSize reset the
	 * mark so that the space can be used.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private void ensureMark(){
		if (marked() >= markSize){
			markPosition = readPosition;
			markSize = 0;
		}
	}

	/**
	 * Create a new buffer with a default capacity.
	 * Writing to a full buffer will block until space
	 * is available rather than throw an exception.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public CircularByteBuffer(){
		this (DEFAULT_SIZE, true);
	}

	/**
	 * Create a new buffer with given capacity.
	 * Writing to a full buffer will block until space
	 * is available rather than throw an exception.
	 * <p>
	 * Note that the buffer may reserve some bytes for
	 * special purposes and capacity number of bytes may
	 * not be able to be written to the buffer.
	 * <p>
	 * Note that if the buffer is of INFINITE_SIZE it will
	 * neither block or throw exceptions, but rather grow
	 * without bound.
	 *
	 * @param size desired capacity of the buffer in bytes or CircularByteBuffer.INFINITE_SIZE.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public CircularByteBuffer(int size){
		this (size, true);
	}

	/**
	 * Create a new buffer with a default capacity and
	 * given blocking behavior.
	 *
	 * @param blockingWrite true writing to a full buffer should block
	 *        until space is available, false if an exception should
	 *        be thrown instead.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public CircularByteBuffer(boolean blockingWrite){
		this (DEFAULT_SIZE, blockingWrite);
	}

	/**
	 * Create a new buffer with the given capacity and
	 * blocking behavior.
	 * <p>
	 * Note that the buffer may reserve some bytes for
	 * special purposes and capacity number of bytes may
	 * not be able to be written to the buffer.
	 * <p>
	 * Note that if the buffer is of INFINITE_SIZE it will
	 * neither block or throw exceptions, but rather grow
	 * without bound.
	 *
	 * @param size desired capacity of the buffer in bytes or CircularByteBuffer.INFINITE_SIZE.
	 * @param blockingWrite true writing to a full buffer should block
	 *        until space is available, false if an exception should
	 *        be thrown instead.
	 *
	 * @since ostermillerutils 1.00.00
	 */
        
	public CircularByteBuffer(int size, boolean blockingWrite){
		if (size == INFINITE_SIZE){
			buffer = new byte[DEFAULT_SIZE];
			infinite = true;
		} else {
			buffer = new byte[size];
			infinite = false;
		}
		this.blockingWrite = blockingWrite;
	}

	/**
	 * Class for reading from a circular byte buffer.
	 *
	 * @since ostermillerutils 1.00.00
	 */
        
	protected class CircularByteBufferInputStream extends InputStream {

		/**
		 * Returns the number of bytes that can be read (or skipped over) from this
		 * input stream without blocking by the next caller of a method for this input
		 * stream. The next caller might be the same thread or or another thread.
		 *
		 * @return the number of bytes that can be read from this input stream without blocking.
		 * @throws IOException if the stream is closed.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		@Override public int available() throws IOException {
			lock.lock();
                        try{
				if (inputStreamClosed) throw new IOException("InputStream has been closed, it is not ready.");
				return (CircularByteBuffer.this.available());
			}finally{lock.unlock();}
		}

		/**
		 * Close the stream. Once a stream has been closed, further read(), available(),
		 * mark(), or reset() invocations will throw an IOException. Closing a
		 * previously-closed stream, however, has no effect.
		 *
		 * @throws IOException never.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		@Override public void close() throws IOException {
			lock.lock();
                        try{
				inputStreamClosed = true;
                                notEnoughtDataToRead.signal();
			}finally{lock.unlock();}
		}

		/**
		 * Mark the present position in the stream. Subsequent calls to reset() will
		 * attempt to reposition the stream to this point.
		 * <p>
		 * The readAheadLimit must be less than the size of circular buffer, otherwise
		 * this method has no effect.
		 *
		 * @param readAheadLimit Limit on the number of bytes that may be read while
		 *    still preserving the mark. After reading this many bytes, attempting to
		 *    reset the stream will fail.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		@Override public void mark(int readAheadLimit) {
			lock.lock();
                        try{
				//if (inputStreamClosed) throw new IOException("InputStream has been closed; cannot mark a closed InputStream.");
				if (buffer.length - 1 > readAheadLimit) {
					markSize = readAheadLimit;
					markPosition = readPosition;
				}
			}finally{lock.unlock();}
		}

		/**
		 * Tell whether this stream supports the mark() operation.
		 *
		 * @return true, mark is supported.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		@Override public boolean markSupported() {
			return true;
		}

		/**
		 * Read a single byte.
		 * This method will block until a byte is available, an I/O error occurs,
		 * or the end of the stream is reached.
		 *
		 * @return The byte read, as an integer in the range 0 to 255 (0x00-0xff),
		 *     or -1 if the end of the stream has been reached
		 * @throws IOException if the stream is closed.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		@Override public int read() throws IOException {
//			while (true){
//				synchronized (CircularByteBuffer.this){
//					if (inputStreamClosed) throw new IOException("InputStream has been closed; cannot read from a closed InputStream.");
//					int available = CircularByteBuffer.this.available();
//					if (available > 0){
//						int result = buffer[readPosition] & 0xff;
//						readPosition++;
//						if (readPosition == buffer.length){
//							readPosition = 0;
//						}
//						ensureMark();
//						return result;
//					} else if (outputStreamClosed){
//						return -1;
//					}
//				}
//				try {
//					Thread.sleep(100);
//				} catch(Exception x){
//					throw new IOException("Blocking read operation interrupted.");
//				}
//			}
                    throw new RuntimeException("Not implemented");
		}

		/**
		 * Read bytes into an array.
		 * This method will block until some input is available,
		 * an I/O error occurs, or the end of the stream is reached.
		 *
		 * @param cbuf Destination buffer.
		 * @return The number of bytes read, or -1 if the end of
		 *   the stream has been reached
		 * @throws IOException if the stream is closed.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		@Override public int read(byte[] cbuf) throws IOException {
                    int off=0;
                    lock.lock();
                    if (DEBUG==true) System.out.println("LETTORE-->LOCK");
//                           if (DEBUG==true) System.out.println("LETTORE-->PRIMA");
//                            if (DEBUG==true) System.out.println("ReadPos--> "+readPosition);
//                            if (DEBUG==true) System.out.println("WritePpo--> "+writePosition);
//                            if (DEBUG==true) System.out.println("MarkPos--> "+markPosition);
                    try{
                        if (inputStreamClosed) throw new IOException("InputStream has been closed; cannot read from a closed InputStream.");
                        if (outputStreamClosed){
                                //I read nothing since stream was closed
                                //return without waking up...
                                return -1;
                        }
                        int available = CircularByteBuffer.this.available();
                        //Reader has nothing to read, so wait for some data
//                        if (available==0) return 0;
                        if (available==0){
                             try{
                                 if (DEBUG==true) System.out.println("LETTORE-->0 byte da leggere (dormo)");
                                 notEnoughtDataToRead.await();
                                 if (DEBUG==true) System.out.println("LETTORE-->risvegliato");
                                 available = CircularByteBuffer.this.available();
                             }catch(InterruptedException ignore){}
                        }
                        // Qui ci sono dati da leggere;
                        int length = Math.min(cbuf.length, available);
                        int firstLen = Math.min(length, buffer.length - readPosition);
                        int secondLen = length - firstLen;
                        System.arraycopy(buffer, readPosition, cbuf, off, firstLen);
                        if (secondLen > 0){
                                System.arraycopy(buffer, 0, cbuf, off+firstLen,  secondLen);
                                readPosition = secondLen;
                                //in fact in this case we have secondLen elements in the
                                //first half of the buffer, the last element has index
                                // secondLen-1, so the first position to read is secondLen
                        } else {
                                readPosition += length;
                        }
                        if (readPosition == buffer.length) {
                                readPosition = 0;
                        }
                        //ensureMark();
                        if (DEBUG==true) System.out.println("LETTORE--> "+length+" byte letti");
                        // Reader removed something so wake up Producer
                        notEnoughtFreeSpace.signal();
                        //Return and unlock
//                        if (DEBUG==true) System.out.println("LETTORE-->DOPO");
//                        if (DEBUG==true) System.out.println("ReadPos--> "+readPosition);
//                        if (DEBUG==true) System.out.println("WritePpo--> "+writePosition);
//                        if (DEBUG==true) System.out.println("MarkPos--> "+markPosition);
                        return length;
                        }
                        // Stream was open but no data to read
                    finally
                    {
                        if (DEBUG==true)  System.out.println("LETTORE-->UNLOCK");
                        lock.unlock();
                    }
                }

		/**
		 * Read bytes into a portion of an array.
		 * This method is non-blocking. Returns always immidiatly
		 * Returns immidiatly I/O error occurs, or the end of the stream is reached.
		 *
		 * @param cbuf Destination buffer.
		 * @param off Offset at which to start storing bytes.
		 * @param len Maximum number of bytes to read.
		 * @return The number of bytes read, or -1 if the end of
		 *   the stream has been reached
		 * @throws IOException if the stream is closed.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		@Override public int read(byte[] cbuf, int off, int len) throws IOException {
                    lock.lock();
                    if (DEBUG==true) System.out.println("LETTORE-->LOCK"); 
//                           if (DEBUG==true) System.out.println("LETTORE-->PRIMA");
//                            if (DEBUG==true) System.out.println("ReadPos--> "+readPosition);
//                            if (DEBUG==true) System.out.println("WritePpo--> "+writePosition);
//                            if (DEBUG==true) System.out.println("MarkPos--> "+markPosition);
                    try{
                        if (inputStreamClosed) throw new IOException("InputStream has been closed; cannot read from a closed InputStream.");
                        if (outputStreamClosed){
                                //I read nothing since stream was closed
                                //return without waking up...
                                return -1;
                        }
                        int available = CircularByteBuffer.this.available();
                        //Reader has nothing to read, so wait for some data
//                        if (available==0) return 0;
                        if (available==0){
                            try {
                                if (DEBUG == true) {
                                    System.out.println("LETTORE-->0 byte da leggere (dormo)");
                                }
                                notEnoughtDataToRead.await();
                                if (DEBUG == true) {
                                    System.out.println("LETTORE-->risvegliato");
                                }
                                available = CircularByteBuffer.this.available();
                                if(available==0){
                                    //I've been waken up because input stream has been
                                    //closed.
                                    return -1;
                                }
                            } catch (InterruptedException ignore) {
                            }
                        }
                        // Qui ci sono dati da leggere;
                        int length = Math.min(len, available);
                        int firstLen = Math.min(length, buffer.length - readPosition);
                        int secondLen = length - firstLen;
                        System.arraycopy(buffer, readPosition, cbuf, off, firstLen);
                        if (secondLen > 0){
                                System.arraycopy(buffer, 0, cbuf, off+firstLen,  secondLen);
                                readPosition = secondLen;
                                //in fact in this case we have secondLen elements in the
                                //first half of the buffer, the last element has index
                                // secondLen-1, so the first position to read is secondLen
                        } else {
                                readPosition += length;
                        }
                        if (readPosition == buffer.length) {
                                readPosition = 0;
                        }
                        //ensureMark();
                        if (DEBUG==true) System.out.println("LETTORE--> "+length+" byte letti");
                        // Reader removed something so wake up Producer
                        notEnoughtFreeSpace.signal();
                        //Return and unlock
//                        if (DEBUG==true) System.out.println("LETTORE-->DOPO");
//                        if (DEBUG==true) System.out.println("ReadPos--> "+readPosition);
//                        if (DEBUG==true) System.out.println("WritePpo--> "+writePosition);
//                        if (DEBUG==true) System.out.println("MarkPos--> "+markPosition);
                        return length;
                        }
                        // Stream was open but no data to read 
                    finally
                    {
                        if (DEBUG==true)  System.out.println("LETTORE-->UNLOCK");
                        lock.unlock();
                    }
		}

		/**
		 * Reset the stream.
		 * If the stream has been marked, then attempt to reposition i
		 * at the mark. If the stream has not been marked, or more bytes
		 * than the readAheadLimit have been read, this method has no effect.
		 *
		 * @throws IOException if the stream is closed.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		@Override public void reset() throws IOException {
			lock.lock();
                        try{
				if (inputStreamClosed) throw new IOException("InputStream has been closed; cannot reset a closed InputStream.");
				readPosition = markPosition;
			}finally{lock.unlock();}
		}

		/**
		 * Skip bytes.
		 * This method will block until some bytes are available,
		 * an I/O error occurs, or the end of the stream is reached.
		 *
		 * @param n The number of bytes to skip
		 * @return The number of bytes actually skipped
		 * @throws IllegalArgumentException if n is negative.
		 * @throws IOException if the stream is closed.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		@Override public long skip(long n) throws IOException, IllegalArgumentException {
                    throw new RuntimeException("Not yet implemented");
		}
	}

	/**
	 * Class for writing to a circular byte buffer.
	 * If the buffer is full, the writes will either block
	 * until there is some space available or throw an IOException
	 * based on the CircularByteBuffer's preference.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected class CircularByteBufferOutputStream extends OutputStream {

		/**
		 * Close the stream, flushing it first.
		 * This will cause the InputStream associated with this circular buffer
		 * to read its last bytes once it empties the buffer.
		 * Once a stream has been closed, further write() or flush() invocations
		 * will cause an IOException to be thrown. Closing a previously-closed stream,
		 * however, has no effect.
		 *
		 * @throws IOException never.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		@Override public void close() throws IOException {
			lock.lock();
                        try{
				if (!outputStreamClosed){
					flush();
				}
				outputStreamClosed = true;
			}finally{lock.unlock();}
		}

		/**
		 * Flush the stream.
		 *
		 * @throws IOException if the stream is closed.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		@Override public void flush() throws IOException {
			if (outputStreamClosed) throw new IOException("OutputStream has been closed; cannot flush a closed OutputStream.");
			if (inputStreamClosed) throw new IOException("Buffer closed by inputStream; cannot flush.");
			// this method needs to do nothing
		}

		/**
		 * Write an array of bytes.
		 * If the buffer allows blocking writes, this method will block until
		 * all the data has been written rather than throw an IOException.
		 *
		 * @param cbuf Array of bytes to be written
		 * @throws BufferOverflowException if buffer does not allow blocking writes
		 *   and the buffer is full.  If the exception is thrown, no data
		 *   will have been written since the buffer was set to be non-blocking.
		 * @throws IOException if the stream is closed, or the write is interrupted.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		@Override public void write(byte[] cbuf) throws IOException {
			write(cbuf, 0, cbuf.length);
		}

		/**
		 * Write a portion of an array of bytes.
		 * If the buffer allows blocking writes, this method will block until
		 * all the data has been written rather than throw an IOException.
		 *
		 * @param cbuf Array of bytes
		 * @param off Offset from which to start writing bytes
		 * @param len - Number of bytes to write
		 * @throws BufferOverflowException if buffer does not allow blocking writes
		 *   and the buffer is full.  If the exception is thrown, no data
		 *   will have been written since the buffer was set to be non-blocking.
		 * @throws IOException if the stream is closed, or the write is interrupted.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		@Override public void write(byte[] cbuf, int off, int len) throws IOException {
                    lock.lock();
                    if (DEBUG==true)  System.out.println("SCRITTORE-->LOCK");
                    if (DEBUG==true)  System.out.println("SCRITTORE--> "+len+" byte da scrivere");
                    try{
                        while (len > 0){
//                            if (DEBUG==true) System.out.println("SCRITTORE-->PRIMA");
//                            if (DEBUG==true) System.out.println("ReadPos--> "+readPosition);
//                            if (DEBUG==true) System.out.println("WritePpo--> "+writePosition);
//                            if (DEBUG==true) System.out.println("MarkPos--> "+markPosition);
					if (outputStreamClosed) throw new IOException("OutputStream has been closed; cannot write to a closed OutputStream.");
					if (inputStreamClosed) throw new IOException("Buffer closed by InputStream; cannot write to a closed buffer.");
					int spaceLeft = spaceLeft();
					while (infinite && spaceLeft < len){
						resize();
						spaceLeft = spaceLeft();
					}
                                        if (DEBUG==true)  System.out.println("SCRITTORE--> "+spaceLeft+" byte disponibili per la scrittura");
                                        if (spaceLeft==0){
                                           if (DEBUG==true)  System.out.println("SCRITTORE-->dormo(buffer pieno)");
                                            try{
                                                notEnoughtFreeSpace.await();
                                                spaceLeft = spaceLeft();
                                           if (DEBUG==true)      System.out.println("SCRITTORE-->svegliato");
                                            }catch(InterruptedException e){}
                                        }
                                        // mi sveglio e ho dello spazio da scrivere
					if (!blockingWrite && spaceLeft < len) throw new BufferOverflowException("CircularByteBuffer is full; cannot write " + len + " bytes");
					int realLen = Math.min(len, spaceLeft);
					int firstLen = Math.min(realLen, buffer.length - writePosition);
					int secondLen = Math.min(realLen - firstLen, buffer.length - markPosition - 1);
					int written = firstLen + secondLen;
					if (firstLen > 0){
						System.arraycopy(cbuf, off, buffer, writePosition, firstLen);
					}
					if (secondLen > 0){
						System.arraycopy(cbuf, off+firstLen, buffer, 0, secondLen);
						writePosition = secondLen;
					} else {
						writePosition += written;
					}
					if (writePosition == buffer.length) {
						writePosition = 0;
					}
					off += written;
					len -= written;
                                        if (DEBUG==true)  System.out.println("SCRITTORE--> "+written+" byte scritti");
                                        notEnoughtDataToRead.signal();
//                            if (DEBUG==true) System.out.println("SCRITTORE-->DOPO");
//                            if (DEBUG==true) System.out.println("ReadPos--> "+readPosition);
//                            if (DEBUG==true) System.out.println("WritePpo--> "+writePosition);
//                            if (DEBUG==true) System.out.println("MarkPos--> "+markPosition);
				if (len > 0){
                                    try {
                                            notEnoughtFreeSpace.await();
                                    } catch(InterruptedException x){
                                        x.printStackTrace();
                                    }
				}
			}
                    }finally{
                        if (DEBUG==true)  System.out.println("SCRITTORE--> UNLOCK");
                        lock.unlock();
                        }
		}

		/**
		 * Write a single byte.
		 * The byte to be written is contained in the 8 low-order bits of the
		 * given integer value; the 24 high-order bits are ignored.
		 * If the buffer allows blocking writes, this method will block until
		 * all the data has been written rather than throw an IOException.
		 *
		 * @param c number of bytes to be written
		 * @throws BufferOverflowException if buffer does not allow blocking writes
		 *   and the buffer is full.
		 * @throws IOException if the stream is closed, or the write is interrupted.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		@Override public void write(int c) throws IOException {
                    throw new RuntimeException("Not Implemented yet");
//			boolean written = false;
//			while (!written){
//				synchronized (CircularByteBufferModified.this){
//					if (outputStreamClosed) throw new IOException("OutputStream has been closed; cannot write to a closed OutputStream.");
//					if (inputStreamClosed) throw new IOException("Buffer closed by InputStream; cannot write to a closed buffer.");
//					int spaceLeft = spaceLeft();
//					while (infinite && spaceLeft < 1){
//						resize();
//						spaceLeft = spaceLeft();
//					}
//					if (!blockingWrite && spaceLeft < 1) throw new BufferOverflowException("CircularByteBuffer is full; cannot write 1 byte");
//					if (spaceLeft > 0){
//						buffer[writePosition] = (byte)(c & 0xff);
//						writePosition++;
//						if (writePosition == buffer.length) {
//							writePosition = 0;
//						}
//						written = true;
//					}
//				}
//				if (!written){
//					try {
//						Thread.sleep(100);
//					} catch(Exception x){
//						throw new IOException("Waiting for available space in buffer interrupted.");
//					}
//				}
//			}
		}
	}

        public class BufferOverflowException extends RuntimeException{
                
                public BufferOverflowException(String s){
                    super(s);
                }
        }
}
