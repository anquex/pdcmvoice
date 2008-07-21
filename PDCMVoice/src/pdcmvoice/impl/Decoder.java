/*
 * Decoder Class
 */

package pdcmvoice.impl;
import static pdcmvoice.impl.Constants.*;

import org.xiph.speex.SpeexDecoder;
import pdcmvoice.codecs.IlbcDecoder;

import java.io.IOException;
import java.io.StreamCorruptedException;

//import com.Ostermiller.util.CircularByteBuffer;
import pdcmvoice.util.CircularByteBuffer;
import java.io.InputStream;
import java.io.OutputStream;



import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;


/**
 * This class recived 20ms compressed voice frames, decodes them and put 
 * decoded audio samples (320 Byte) on an AudioInputStream
 *
 * Uses PipedOutputStream/PipedInputStream for getting AudioInputStream Object
 * Warning: waiting with piped introduces 1s delay (so use available() method)
 * 
 * @author Marco
 */
public class Decoder {
    
    private CircularByteBuffer cBuffer;
    private InputStream input;
    private OutputStream output;
    
    private SpeexDecoder speexDecoder;
    private IlbcDecoder ilbcDecoder;
    private int encodedFormat;
    
    private boolean inited;
    
    private final boolean DEBUG=false;
    
    public Decoder(int format){
        this.encodedFormat=format;
        
        if (encodedFormat==FORMAT_CODE_SPEEX_NB ||
            encodedFormat==FORMAT_CODE_SPEEX_WB )
        {
            speexDecoder=new SpeexDecoder();
        }
        else if (encodedFormat==FORMAT_CODE_iLBC){
            
        }
        else throw new IllegalArgumentException();
        cBuffer=new CircularByteBuffer(161);
        input=cBuffer.getInputStream();
        output=cBuffer.getOutputStream();
    }
    
    public Decoder(int format, boolean recovery){
        this.encodedFormat=format;
        
        if (encodedFormat==FORMAT_CODE_SPEEX_NB ||
            encodedFormat==FORMAT_CODE_SPEEX_WB )
        {
            speexDecoder=new SpeexDecoder();
        }
        else if (encodedFormat==FORMAT_CODE_iLBC){
            
        }
        else throw new IllegalArgumentException();
        cBuffer=new CircularByteBuffer(150000);
        input=cBuffer.getInputStream();
        output=cBuffer.getOutputStream();
    }
    
    public void init(){
        //set encoder and coding parameters according to 
        //selected format
        if(inited) throw new RuntimeException("Init Already Done");
        
        if (encodedFormat==FORMAT_CODE_SPEEX_NB){            
            int sampleRate=16000;
            int mode=0;
            int channels=1;
            boolean enhanced= false;
            speexDecoder.init(mode,       //   mode - (0=NB, 1=WB, 2=UWB)
                              sampleRate, //the number of samples per second.
                              channels,   //(1=mono, 2=stereo, ...)
                              enhanced    // perceptual enhancement
                              );
        }
        else if(encodedFormat==FORMAT_CODE_SPEEX_WB){
            int sampleRate=16000;
            int mode=1;
            int channels=1;
            boolean enhanced= false;
            speexDecoder.init(mode,       //   mode - (0=NB, 1=WB, 2=UWB)
                              sampleRate, //the number of samples per second.
                              channels,   //(1=mono, 2=stereo, ...)
                              enhanced    // perceptual enhancement
                              );
            
        }
        else if(encodedFormat==FORMAT_CODE_iLBC)
        {// to be implemented
            int mode=20; //20 ms
            ilbcDecoder= new IlbcDecoder(mode, true);
        }
        else throw new RuntimeException("Invalid Parameter");
        inited=true;
    }
    /* This method is called from an external object every time a packet
     * is received
     * 
     * @param frame Speex/iLBC audio frame 
     * @param SN RTP associated packet Sequential Number (to be used)
     * @param RTP Timestamp (to be used)
     */
    
    public void decodeFrame(byte[] frame){
        // If decoder is not ready then drop
        if (!inited) return;
        
        if (DEBUG){
            String out="";
            for (int i=0;i<frame.length;i++){
                out+=" "+frame[i];
            }
            out(out);
            }
        
        byte[] PCMFrame=null;
            //Speex Encoding
        if (encodedFormat==FORMAT_CODE_SPEEX_NB ||
            encodedFormat==FORMAT_CODE_SPEEX_WB )
        {   
            try{
            if(frame==null)//frame has been lost
                speexDecoder.processData(true); //lost=true
            else //decode received frame
                speexDecoder.processData(frame, 0, frame.length);
            }catch(StreamCorruptedException e){e.printStackTrace();}
            PCMFrame=new byte[speexDecoder.getProcessedDataByteSize()];
            speexDecoder.getProcessedData(PCMFrame, 0);
       }       // iLBC Encoding
        else{   //to be implemented
            ilbcDecoder.processData(frame, 0, frame.length);
            PCMFrame=new byte[ilbcDecoder.getProcessedDataByteSize()];
            ilbcDecoder.getProcessedData(PCMFrame, 0);
       }
        try{
            output.write(PCMFrame, 0, PCMFrame.length);
        //poutput.write(PCMFrame, 0, PCMFrame.length);
        }catch(IOException ignore){ignore.printStackTrace();}
       
       // System.out.println("Pacchetto decodificato");
    }
    
    public AudioInputStream getAudioInputStream() throws UnsupportedAudioFileException{
        AudioFormat format=AudioUtils.getLineAudioFormat(encodedFormat);
        return new AudioInputStream(input, format, AudioSystem.NOT_SPECIFIED);
    //    return new AudioInputStream(pinput, AudioUtils.getLineAudioFormat(encodedFormat), AudioSystem.NOT_SPECIFIED);
    }
}
