/*
 *	AudioCapture.java
 */

/*
 * Copyright (c) 2001, 2004 by Florian Bomers
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package pdcmvoice.impl;

import	java.io.*;
import	javax.sound.sampled.*;
import static pdcmvoice.impl.Constants.*;

// class that provides an AudioInputStream that reads its data from the soundcard input
// the AudioInputStream is in the network format
public class AudioCapture extends AudioBase {

    private static final boolean DEBUG = true;

    protected AudioInputStream ais;
    protected OutputStream outputStream;
    private CaptureThread thread;

    public AudioCapture(int formatCode, Mixer mixer, int bufferSizeMillis) {
	super("Microphone", formatCode, mixer, bufferSizeMillis);
    }

    protected void createLineImpl() throws Exception {
	DataLine.Info info = new DataLine.Info(TargetDataLine.class, lineFormat);

	// get and open the target data line for capture.
	if (mixer != null) {
	    line = (TargetDataLine) mixer.getLine(info);
	} else {
	    line = AudioSystem.getTargetDataLine(lineFormat);
	}
    }

    protected void openLineImpl() throws Exception {
	TargetDataLine tdl = (TargetDataLine) line;
	tdl.open(lineFormat, bufferSize);
	ais = new AudioInputStream(tdl);
    }

    public synchronized void start() throws Exception {
    	boolean needStartThread = false;
    	if (thread != null && (thread.isTerminating() || outputStream == null)) {
	    thread.terminate();
	    needStartThread = true;
    	}
    	if (VERBOSE) out("AudioCapture: start with OutputStream = "+outputStream);
    	if ((thread == null || needStartThread) && outputStream != null) {
	    // start thread
	    thread = new CaptureThread();
	    thread.start();
	}
	super.start();
    }

    protected void closeLine(boolean willReopen) {
    	CaptureThread oldThread = null;
    	synchronized(this) {
	    if (!willReopen && thread != null) {
		thread.terminate();
	    }
	    super.closeLine(willReopen);
	    if (!willReopen) {
		if (ais != null) {
	            if (VERBOSE) out("AudioCapture.closeLine(): closing input stream");
		    try {
			ais.close();
		    } catch(IOException ioe) {}
		}
		if (thread != null) {
		    if (outputStream != null) {
			try {
			    outputStream.close();
			} catch(IOException ioe) {}
			outputStream = null;
		    }
		    oldThread = thread;
		}
	    }
	}
	if (oldThread != null) {
	    if (VERBOSE) out("AudioCapture.closeLine(): closing thread, waiting for it to die");
	    oldThread.waitFor();
	    if (VERBOSE) out("AudioCapture.closeLine(): thread closed");
	}
    }

    // in network format
    public AudioInputStream getAudioInputStream() {
	return ais;
    }

    /**
     * Set the output stream to write to.
     * Must be set *before* calling start. When
     * writing to the Output Stream, the stream
     * returned by getAudioInputStream must not be read from.
     */
    public synchronized void setOutputStream(OutputStream stream) {
	this.outputStream = stream;
	//Debug.out("AudioCapture.setOutputStream(): output stream: " + this.outputStream);
	if (this.outputStream == null && thread != null) {
	    thread.terminate();
	    thread = null;
	}
    }

    public synchronized OutputStream getOutputStream() {
    	return this.outputStream;
    }


    // thread used for writing the captured audio data to the output stream
    class CaptureThread extends Thread {
	private boolean doTerminate = false;
	private boolean terminated = false;

	public void run() {
	    byte[] buffer = new byte[getBufferSize()];
	    if (VERBOSE) out("Start AudioCapture push thread");
	    try {
	    	AudioInputStream localAIS = ais;
		while (!doTerminate) {
		    if (localAIS != null) {
			int r = localAIS.read(buffer, 0, buffer.length);
			if (r > 0) {
			    synchronized (AudioCapture.this) {
		    		if (outputStream != null) {
				    outputStream.write(buffer, 0, r);
				}
			    }
			    if (outputStream == null) {
				synchronized(this) {
				    this.wait(100);
				}

			    }
			} else {
			    if (r == 0) {
				synchronized(this) {
				    this.wait(20);
				}
			    }
			}
		    } else {
			synchronized(this) {
			    this.wait(50);
			}
		    }
		}
	    } catch (IOException ioe) {
		//if (VERBOSE) ioe.printStackTrace();
	    } catch (InterruptedException ie) {
		if (DEBUG) ie.printStackTrace();
	    }
	    terminated = true;
	    if (VERBOSE) out("Stop AudioCapture push thread");
	}

	public synchronized void terminate() {
	    doTerminate = true;
	    this.notifyAll();
	}

	public synchronized boolean isTerminating() {
		return doTerminate || terminated;
	}

	public synchronized void waitFor() {
	    if (!terminated) {
		try {
		    this.join();
		} catch (InterruptedException ie) {
		    if (DEBUG) ie.printStackTrace();
		}
	    }
	}
    }
}

