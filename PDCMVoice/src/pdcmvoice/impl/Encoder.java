/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pdcmvoice.impl;

import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.xiph.speex.SpeexEncoder;
import static pdcmvoice.impl.Constants.*;
import pdcmvoice.codecs.IlbcEncoder;

/**
 *
 * @author marco
 */
public class Encoder extends Thread {

    private AudioInputStream ais;
    private SpeexEncoder speexEncoder = null;
    private IlbcEncoder ilbcEncoder = null;
    private int encoding_format;
    private Packetizer packetizer; //callback reference
    private boolean registered;
    private boolean inited;
    //encoding parameters;
    public final int DECODEDNB = 320;         // unencoded bytes in frameDurationInMillis
    public final int DECODEDWB = 640;
    private int PCMbytesPerFrame;
    private int nEncoded;
    private int speexquality = SPEEX_QUALITY[DEFAULT_SPEEX_QUALITY_INDEX];
    private final boolean DEBUG = false;
    // debug variables
    private int producedFrames;
    private long firsttimeStamp;
    private long lasttimeStamp;

    public Encoder(int encoded_format_code, AudioInputStream ais) {

        this.ais = ais;
        encoding_format = encoded_format_code;

        if (encoding_format == FORMAT_CODE_SPEEX_NB || encoding_format == FORMAT_CODE_SPEEX_WB) {
            speexEncoder = new SpeexEncoder();

        } else if (encoding_format == FORMAT_CODE_iLBC) {
            int mode = 20; //20ms
            ilbcEncoder = new IlbcEncoder(mode, true);
        } else {
            throw new IllegalArgumentException();
        }

        init();

    }

    /** Initialize encoder according to selected format
     * 
     * All codecs encoded 20ms of PCM audio per encoded frame
     */
    private void init() {
        //set encoder and coding parameters according to 
        //selected format
        if (inited) {
            throw new RuntimeException("Init Already Done");
        }

        if (encoding_format == FORMAT_CODE_SPEEX_NB) {
            int sampleRate = 8000;
            int mode = 0;
            int quality = speexquality;
            int channels = ais.getFormat().getChannels();
            speexEncoder.init(mode, //   mode - (0=NB, 1=WB, 2=UWB)
                    quality, //the quality (between 0 and 10).
                    sampleRate, //the number of samples per second.
                    channels //(1=mono, 2=stereo, ...)
                    );
            PCMbytesPerFrame = DECODEDNB; //20ms audio 
        } else if (encoding_format == FORMAT_CODE_SPEEX_WB) {
            int sampleRate = 16000;
            int mode = 1;
            int quality = speexquality;
            int channels = ais.getFormat().getChannels();
            speexEncoder.init(mode, //   mode - (0=NB, 1=WB, 2=UWB)
                    quality, //the quality (between 0 and 10).
                    sampleRate, //the number of samples per second.
                    channels //(1=mono, 2=stereo, ...)
                    );
            PCMbytesPerFrame = DECODEDWB; //20ms audio 

        } else if (encoding_format == FORMAT_CODE_iLBC) {
            PCMbytesPerFrame = DECODEDNB; //20ms audio 
        } else {
            throw new RuntimeException("Invalid Parameter");
        }

        inited = true;
    }//end init
    
    /** Register the packetizer which would be colled to send voice packets
     * as soon as they ara available
     */
    
    public int registerPacketizer(Packetizer p) {
        if (registered) {
            System.out.println("Encoder: Can\'t register another packetizer!");
            return -1;
        } else {
            registered = true;
            this.packetizer = p;
            return 0;
        }
    }//end registerPacketizer
    
    /** Starts reading the AudioInputStream (blocking call),
     *  encode and notify the packetizer.
     * 
     */
    
    public void run() {
        if (!inited) {
            throw new IllegalStateException();
        }
        if (!registered) {
            throw new NullPointerException();
        }

        //Speex Encoding
        if (encoding_format == FORMAT_CODE_SPEEX_NB ||
                encoding_format == FORMAT_CODE_SPEEX_WB) {
            byte[] buffer = new byte[PCMbytesPerFrame];
            int nReadBytes = 0;
            int encodedDataBytes = 0;
            byte encodedFrame[] = null;
            while (nReadBytes != -1) {
                if (DEBUG) {
                    lasttimeStamp = System.currentTimeMillis();
                    if (firsttimeStamp == 0) {
                        firsttimeStamp = System.currentTimeMillis();
                    } else {
                        out("" + ((lasttimeStamp - firsttimeStamp) / producedFrames));
                    }
                    producedFrames++;
                }
                nEncoded++;
                try {
                    nReadBytes = ais.read(buffer);
//                   System.out.println(nReadBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                    break; //let process die
                }
                if (nReadBytes > 0) {
                    speexEncoder.processData(buffer, 0, nReadBytes);
                    encodedFrame = new byte[speexEncoder.getProcessedDataByteSize()];
                    //    System.out.println(encodedFrame.length);
                    speexEncoder.getProcessedData(encodedFrame, 0);
//                    if (DEBUG){
//                        String out="";
//                        for (int i=0;i<encodedFrame.length;i++){
//                            out+=" "+encodedFrame[i];
//                        }
//                        out(out);
//                    }
                    packetizer.sendVoice(encodedFrame);
                }
            }
        // iLBC Encoding
        } else {
            byte[] buffer = new byte[PCMbytesPerFrame];
            int nReadBytes = 0;
            int encodedDataBytes = 0;
            byte encodedFrame[] = null;
            while (nReadBytes != -1) {
                try {
                    nReadBytes = ais.read(buffer);
                // out(nReadBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                    break; //let process die
                }
                if (nReadBytes > 0) {
                    ilbcEncoder.processData(buffer, 0, nReadBytes);
                    encodedFrame = new byte[ilbcEncoder.getProcessedDataByteSize()];
                    //    System.out.println(encodedFrame.length);
                    ilbcEncoder.getProcessedData(encodedFrame, 0);
                    packetizer.sendVoice(encodedFrame);
                }
            }
        }
    }//end run
    /** Return current speex Encoder quality
     * 
     * 
     * @return speex quality
     */
    
    public int speexQuality() {
        return speexquality;
    }
    
    /**  Set speex quality (only speex encoding is influenced)
     *  Quality can be changed runtime seamlessly
     * 
     *  @return new quality if operation succeded or previous quality
     *  otherwise 
     */

    public int speexQuality(int n) {
        if (n >= 0 && n <= 10) {
            return speexquality = n;
        } else {
            return speexquality;
        }
    }
}//end Encoder

