/*
 *	AudioPlayStream.java
 */

/*
 * Copyright (c) 2001,2004 by Florian Bomers
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
import java.util.logging.Level;
import java.util.logging.Logger;
import	javax.sound.sampled.*;
import static pdcmvoice.impl.Constants.*;

// Class that reads its audio from an AudioInputStream
public class AudioPlayback extends AudioBase {

    private static final boolean DEBUG_TRANSPORT = false;
    private static final boolean RECORDING = true;

    protected AudioInputStream ais;
    private PlayThread thread;
    SourceDataLine sdl;

    private SpeakerRecoder sr;

    public AudioPlayback(int formatCode, Mixer mixer, int bufferSizeMillis) {
	super("Speaker", formatCode, mixer, bufferSizeMillis);
    }

    protected void createLineImpl() throws Exception {
	DataLine.Info info = new DataLine.Info(SourceDataLine.class, lineFormat);

	// get the playback data line for capture.
	if (mixer != null) {
	    line = (SourceDataLine) mixer.getLine(info);
	} else {
	    line = AudioSystem.getSourceDataLine(lineFormat);
	}
    }

    protected void openLineImpl() throws Exception {
	SourceDataLine sdl = (SourceDataLine) line;
	sdl.open(lineFormat, bufferSize);
    }

    public synchronized void start() throws Exception {
    	boolean needStartThread = false;
    	if (thread != null && thread.isTerminating()) {
	    thread.terminate();
	    needStartThread = true;
    	}
    	if (thread == null || needStartThread) {
		// start thread
		thread = new PlayThread();
		thread.start();
	}
	super.start();
    }

    protected void closeLine(boolean willReopen) {
    	PlayThread oldThread = null;
    	synchronized(this) {
	if (!willReopen && thread != null) {
	    thread.terminate();
	}
	super.closeLine(willReopen);
	if (!willReopen && thread != null) {
	    oldThread = thread;
	    thread = null;
	}
	}
	if (oldThread != null) {
	    if (VERBOSE) out("AudioPlayback.closeLine(): closing thread, waiting for it to die");
	    oldThread.waitFor();
	    if (VERBOSE) out("AudioPlayback.closeLine(): thread closed");
	}
    }

    // in network format
    public void setAudioInputStream(AudioInputStream ais) {
	this.ais = AudioSystem.getAudioInputStream(lineFormat, ais);
    }

    class PlayThread extends Thread {
	private boolean doTerminate = false;
	private boolean terminated = false;
	// for debugging
	private boolean printedBytes = false;


	public void run() {
	    if (VERBOSE) out("Start AudioPlayback pull thread");
	    byte[] buffer = new byte[getBufferSize()];
            sdl = (SourceDataLine) line;
//            Meter m=new Meter();
//            m.start();
            if (RECORDING){
                sr= new SpeakerRecoder();
                sr.start();
            }
	    try {
		while (!doTerminate) {
                    //out("do terminate? "+doTerminate);
		    if (ais != null) {
			int r = ais.read(buffer, 0, buffer.length);
			if (r > 50 && DEBUG_TRANSPORT && !printedBytes) {
			    printedBytes = true;
			    out("AudioPlayback: first bytes being played:");
			    String s = "";
			    for (int i = 0; i < 50; i++) {
				s+=" "+buffer[i];
			    }
			    out(s);
			}
			if (r > 0) {
			    if (isMuted()) {
				muteBuffer(buffer, 0, r);
			    }
			    // run some simple analysis
			    calcCurrVol(buffer, 0, r);
			    if (sdl != null) {
			    	sdl.write(buffer, 0, r);
                                if (RECORDING)
                                    sr.write(buffer, 0, r);
			    }
			} else {
			    if (r == 0) {
				synchronized(this) {
				    this.wait(40);
				}
			    }
			}
		    } else {
			synchronized(this) {
			    this.wait(50);
			}
		    }
		}
                sdl.drain();
                sdl.flush();
	    } catch (IOException ioe) {
		//if (DEBUG) ioe.printStackTrace();
	    } catch (InterruptedException ie) {
		if (DEBUG) ie.printStackTrace();
	    }
	    if (VERBOSE) out("Stop AudioPlayback pull thread");
	    terminated = true;
	}

	public synchronized void terminate() {
	    doTerminate = true;
            try{
                ais.close();
            }catch(IOException e){e.printStackTrace();}
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

    class Meter extends Thread{

        public void run() {
        try{
        while(true){
            String out="";
            int d1,d2=0,d3=0;
                { if (AudioPlayback.this.thread==null)
                      break;
                  //  d1=target.available();
                    if (ais!=null)
                    d2=ais.available();
                    d3=getBufferSize()-sdl.available();
                  //  out+="TARTGET :"+d1+" - ";
                    out+="PIPE :"+d2+" - ";
                    out+="SOURCE :"+d3+" - ";
                    out+="TOTAL :"+(+d2+d3);
             }
            System.out.println(out);
            sleep(500);

            }
        }catch(Exception e){e.printStackTrace();}
        }
    }

    class SpeakerRecoder extends Thread{

        AudioFileFormat.Type fileTypeWAV = null;
        File audioFileWAV = null;
        PipedInputStream in;
        PipedOutputStream out;


        public void run(){
            in= new PipedInputStream();
            out=new PipedOutputStream();
            try {
                in.connect(out);
            } catch (IOException ex) {
                Logger.getLogger(AudioPlayback.class.getName()).log(Level.SEVERE, null, ex);
            }
            AudioFormat format=line.getFormat();
            AudioInputStream ais=new AudioInputStream(in, format, AudioSystem.NOT_SPECIFIED);
            fileTypeWAV = AudioFileFormat.Type.WAVE;
            audioFileWAV = new File("C:\\lastSpeaker.wav");
            try {
                AudioSystem.write(ais, fileTypeWAV, audioFileWAV);
            } catch (IOException ex) {
                Logger.getLogger(AudioPlayback.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void write(byte[] b, int offset, int len){
            try {
                out.write(b, offset, len);
            } catch (IOException ex) {
                Logger.getLogger(AudioPlayback.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
