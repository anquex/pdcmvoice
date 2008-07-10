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
import	javax.sound.sampled.*;
import static pdcmvoice.impl.Constants.*;
import static pdcmvoice.impl.AudioUtils.*;

// Class that reads its audio from an AudioInputStream
public class AudioPlayback extends AudioBase {

    private static final boolean DEBUG_TRANSPORT = false;

    protected AudioInputStream ais;
    private PlayThread thread;

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
        //convert to a higher sample rate to solve 
        // problems of delay that occurs on some computers
        // due to bad drivers
        
//        float outSampleRate=16000.0f;
//        AudioFormat targetFormat=getLineAudioFormat(outSampleRate);
//	this.ais = AudioSystem.getAudioInputStream(targetFormat, ais);
        this.ais=ais;
    }

    class PlayThread extends Thread {
	private boolean doTerminate = false;
	private boolean terminated = false;
	// for debugging
	private boolean printedBytes = false;

	public void run() {
	    if (VERBOSE) out("Start AudioPlayback pull thread");
	    byte[] buffer = new byte[getBufferSize()];
            SourceDataLine sdl = (SourceDataLine) line;
            System.out.println("AudioInputSTREAM :"+ais.getFormat().toString());
            System.out.println("Line Format :"+lineFormat);
            int dropped=0;
            int received=0;
	    try {
		while (!doTerminate) {
		    if (ais != null) {
			int r = ais.read(buffer, 0, buffer.length);
//                        received++;
			if (r > 50 && DEBUG_TRANSPORT && !printedBytes) {
			    printedBytes = true;
			    out("AudioPlayback: first bytes being played:");
			    String s = "";
			    for (int i = 0; i < 50; i++) {
				s+=" "+buffer[i];
			    }
			    out(s);
			}
                        if (isMuted()) {
                            muteBuffer(buffer, 0, r);
                        }
                        // run some simple analysis
                        calcCurrVol(buffer, 0, r);
                        if (sdl != null) {
                                
// try to see how many data put in buffer to avoid blocking calls...
// this is the only stable solution I found for getting a bounded delay...
// but introduces a bit of distortion sometimes...
//                            System.out.println("SPEAKERS: "+sdl.available()+" writable without blocking");
//                            System.out.println("SPEAKERS: "+r+" Byte Read");
// resolves overload of audio device
//                        if (sdl.available()<r)
//                        {   dropped++;
//                            System.out.println("SPEAKERS: drop"+dropped);
//                            System.out.println("SPEAKERS: received"+received);
//                        }
                            sdl.write(buffer, 0, r);
// audio frame size resulted to be the better compromise to wait
// for reducing error introduced by Math.min(sdl.available(), r)
// this helps in computers where java works bad and mantains the 
// same good performance in computers with good drivers
                                
//                            synchronized(this) {
//                                this.wait(20);
//                            }
                        }
		    } else {
			synchronized(this) {
			    this.wait(50);
			}
		    }
		}
	    } catch (IOException ioe) {
		if (DEBUG) ioe.printStackTrace();
	    } catch (InterruptedException ie) {
		if (DEBUG) ie.printStackTrace();
	    }
	    if (VERBOSE) out("Stop AudioPlayback pull thread");
	    terminated = true;
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
