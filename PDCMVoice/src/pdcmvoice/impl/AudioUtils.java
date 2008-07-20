/*
 *	AudioUtils.java
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

//TODO: enable dynamic change of audio format - especially for owner client...

package pdcmvoice.impl;

import java.io.InputStream;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import static pdcmvoice.impl.Constants.*;

import org.xiph.speex.spi.SpeexEncoding;
import net.java.sip.communicator.impl.media.codec.audio.ilbc.*;

public class AudioUtils {
    
    public static final AudioFormat.Encoding ILBC=new AudioFormat.Encoding("iLBC");


    public static long bytes2millis(long bytes, AudioFormat format) {
	return (long) (bytes/format.getFrameRate()*1000/format.getFrameSize());
    }

    public static long millis2bytes(long ms, AudioFormat format) {
	return (long) (ms*format.getFrameRate()/1000*format.getFrameSize());
    }


    public static AudioFormat getLineAudioFormat(float sampleRate) {
	return new AudioFormat(
			       AudioFormat.Encoding.PCM_SIGNED,
			       sampleRate,    // sampleRate
			       16,            // sampleSizeInBits
			       1,             // channels
			       2,             // frameSize
			       sampleRate,    // frameRate
			       false);        // bigEndian
    }
        public static AudioFormat getLineAudioFormat(int formatcode) throws UnsupportedAudioFileException{
        float sampleRate= AudioUtils.getNetAudioFormat(formatcode).getSampleRate();
        return AudioUtils.getLineAudioFormat(sampleRate);
    }

    public static AudioFormat getNetAudioFormat(int formatCode) throws UnsupportedAudioFileException {
        // add quality propriety to SPEEX audio format
        Map<String,Object> proprieties=new LinkedHashMap<String,Object>();
        proprieties.put("quality", SPEEX_QUALITY[DEFAULT_SPEEX_QUALITY_INDEX]);
        
	if (formatCode==FORMAT_CODE_SPEEX_NB) {
	    return new AudioFormat(
				   SpeexEncoding.SPEEX,
				   8000.0f, // sampleRate
				   -1,      // sampleSizeInBits
				    1,      // channels
				   -1,      // frameSize
				   -1,      // frameRate
				   true,  // bigEndian
                                   proprieties);
	}
	else if (formatCode==FORMAT_CODE_SPEEX_WB) {
	    return new AudioFormat(
				   SpeexEncoding.SPEEX,
				   16000.0f,// sampleRate
				   -1,      // sampleSizeInBits
				    1,      // channels
				   -1,      // frameSize
				   -1,      // frameRate
				   true,  // bigEndian
                                   proprieties);
	}
	else if (formatCode==FORMAT_CODE_iLBC) {
	    return new AudioFormat(
                ILBC,
                8000.0f, //sampleRate
                -1,     //sampleSizeInBits
                1,      //channels
                -1,     // frameSize
                -1,      // frameRate
                false //bigEndian
                );
	}
	else throw new RuntimeException("Wrong format code!");
    }
    
    /* Build an AudioInputStream from the given InputStream and format
     * 
     */

    public static AudioInputStream createNetAudioInputStream(
                        	int formatCode, InputStream stream) 
    {
	try
	{
	    AudioFormat format = getNetAudioFormat(formatCode);
	    return new AudioInputStream(stream, format, AudioSystem.NOT_SPECIFIED);
	}
	catch (UnsupportedAudioFileException e)
	{
	    Debug.out(e);
	    return null;
	}
    }


    public static int getFormatCode(AudioFormat format) {
	AudioFormat.Encoding encoding = format.getEncoding();
	if (encoding.equals(SpeexEncoding.SPEEX)) {
            if (format.getSampleRate()==8000.0f)
	    return FORMAT_CODE_SPEEX_NB;
            if (format.getSampleRate()==16000.0f)
            return FORMAT_CODE_SPEEX_WB;
	}
	if (encoding.equals(ILBC)) {
	    return FORMAT_CODE_iLBC;
	}
	throw new RuntimeException("Wrong Format");
    }
    /**
     *  Get the Payload type associated with the econding format
     *  Returns simple (not RDT) payload format.
     *  
     * 
     * @param formatCode encoding format
     * @return associated default payload
     */
    
    public static int getPayloadType(int formatCode){
        if (formatCode==FORMAT_CODE_SPEEX_NB
             || formatCode==FORMAT_CODE_SPEEX_WB)
             return  PAYLOAD_SPEEX;
        if (formatCode==FORMAT_CODE_iLBC)
            return PAYLOAD_iLBC;
        throw new IllegalArgumentException("Format Code doesn\'t have  a defined Payload Type");
    }
    
}

