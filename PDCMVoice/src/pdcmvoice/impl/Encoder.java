/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pdcmvoice.impl;

import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import org.xiph.speex.SpeexEncoder;
import static pdcmvoice.impl.Constants.*;
import pdcmvoice.codecs.IlbcEncoder;

/**
 *
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
    private int speexquality = SPEEX_QUALITIES[DEFAULT_SPEEX_QUALITY_INDEX];

    private final boolean DEBUG = false;
    private final boolean VERBOSE = false;

    
    private int producedFrames;
    private int producedBytes;
    private int lastFrameSize;
    //DEBUG
//    private long firsttimeStamp;
//    private long lasttimeStamp;
//    private int actionRunning;
    

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

    }

    /** Initialize encoder according to selected format
     *
     *  This allows for changing encoding settings (such as SPEEX.QUALITY
     *  before starting encoding. Once encoding is started changing is ignored.
     *
     *  All codecs encoded 20ms of PCM audio per encoded frame
     */

    public void init() {
        //set encoder and coding parameters according to 
        //selected format
        out("Encoder Inited");
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
            throw new RuntimeException("No Packetizer registered");
        }

        byte[] buffer = new byte[PCMbytesPerFrame];
        int nReadBytes = -1;
        byte encodedFrame[] = null;
        out("Encoder: Encoding Started...");
            while (nReadBytes != 0) {
                // READ FROM THE STREAM 20 MS OF AUDIO
//                long t=System.currentTimeMillis();
                try {
                    nReadBytes = ais.read(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                    break; //let process die
                }
                // ENCODE READED BYTES
                if (DEBUG && VERBOSE)
                    out("Encoder Read: "+ nReadBytes+" Bytes");
                if (nReadBytes >= PCMbytesPerFrame) {
                    // encoding need at least 20 ms audio
                    // if stream is closed I read less then required bytes...
                    // so I ignore last incomplete frame

                    //Speex Encoding
                    if (encoding_format == FORMAT_CODE_SPEEX_NB ||
                        encoding_format == FORMAT_CODE_SPEEX_WB)
                    {
//                        long t=System.currentTimeMillis();
                        speexEncoder.processData(buffer, 0, nReadBytes);
                        encodedFrame = new byte[speexEncoder.getProcessedDataByteSize()];
                        speexEncoder.getProcessedData(encodedFrame, 0);
//                        out("Encoding took "+(System.currentTimeMillis()-t));
                    }
                    else
                    // iLBC Encoding
                    {
                        ilbcEncoder.processData(buffer, 0, nReadBytes);
                        encodedFrame = new byte[ilbcEncoder.getProcessedDataByteSize()];
                        ilbcEncoder.getProcessedData(encodedFrame, 0);
                    }
//                  long t=System.currentTimeMillis();
//                    Action a= new Action(encodedFrame);
//                    a.start();
                    packetizer.sendVoice(encodedFrame);
//                  out("Sending took "+(System.currentTimeMillis()-t));
//                  out("Total time was took "+(System.currentTimeMillis()-t));

                    // UPDATE STATS
                    producedBytes+=encodedFrame.length;
                    producedFrames++;
                    lastFrameSize=encodedFrame.length;

                    if (DEBUG){
                        String out="Frame "+producedFrames+" lenght "+encodedFrame.length+" Content: ";
                        for (int i=0;i<encodedFrame.length;i++){
                            out+=" "+encodedFrame[i];
                        }
                        out(out);
                    }
                }
            }
            out("Encoder: Encoding Stopped...");


    }//end run
    /** Return current speex Encoder quality
     * 
     * 
     * @return speex quality
     */
    
//    class Action extends Thread{
//        byte[] b;
//        
//        Action(byte[] b){
//            this.b=b;
//        }
//        public void run(){
//            synchronized(Encoder.class){
//                actionRunning++;
//            }
//            out("Action Running "+actionRunning);
//            packetizer.sendVoice(b);
//            synchronized(Encoder.class){
//                actionRunning--;
//            }
//        }
//    }
    public int getSpeexQuality() {
        return speexquality;
    }
    
    /**  Set speex quality (only speex encoding is influenced)
     *  Quality can be changed runtime seamlessly
     * 
     *  @return new quality if operation succeded or previous quality
     *  otherwise 
     */

    public int setSpeexQuality(int n) {
        if (n >= 0 && n <= 10) {
            if (speexquality!=n){
                speexquality = n;
            }
            return speexquality;
        } else {
            return speexquality;
        }
    }

    // FOR STATS PURPOSE
    public int getEncodedBytes(){
        return producedBytes;
    }
    public int getProducedFrames(){
        return producedFrames;
    }
    public int getLastFrameSize(){
        // could return 0 if no frame produced
        return lastFrameSize;
    }

}//end Encoder

