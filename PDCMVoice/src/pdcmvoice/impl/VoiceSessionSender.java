/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.impl;

import javax.sound.sampled.AudioInputStream;
import jlibrtp.RTPSession;

/**
 *
 * @author marco
 */

public class VoiceSessionSender {
    
    private Encoder encoder;
    private RTPSession rtpsession;
    private Packetizer packetizer;
    private AudioCapture capture;
    private int formatCode;
    private boolean RDTForced; // RDT enable/disable RDT autoactivation
    
    public VoiceSessionSender(int formatCode, RTPSession rtp){
        
        this.formatCode=formatCode;
        this.rtpsession=rtp;
        
        // Set rtpsession payload type according to format code
        // This Setting must be performed before creating the Packetizer
        
        rtpsession.payloadType(AudioUtils.getPayloadType(formatCode));
        
        capture= new AudioCapture(formatCode, //fortmato in cui codificare
                                  null, //default mixer
                                  50);  //
        
        packetizer= new Packetizer(rtp);
        try{
            capture.open();
        }catch(Exception e){e.printStackTrace();}
        
        // Connect modules
        
        AudioInputStream ais=capture.getAudioInputStream();
        encoder=new Encoder(formatCode, ais);
        encoder.registerPacketizer(packetizer);
        

//        packetizer.framesPerPackets(2);

//        packetizer.enableRDT();
    }

    
    /**
     *    Acquire audio, encode and send
     */
    
    public void start() throws Exception{
            capture.start();
            encoder.start();
        }

    public void stop(){
        capture.closeLine(false);
        
    }
    /**
     * Returns the packetizer used by this Voice Session
     * 
     * @return packetizer
     */
    public Packetizer getPacketizer(){
        return packetizer;
    }
    /**
     * 
     * Sets RDT to be alsways active or disabled
     * @param enabled true if you want RDT always active
     * @return RDT status after this call
     */
    
    public synchronized boolean manualRDT(boolean enabled){
        RDTForced=true;
        if (enabled)
            packetizer.enableRDT();
        else
            packetizer.disableRDT();
        return isRDT();
    }
    /**
     *  Enable auto RDT keeping prevois state
     * @return
     */
     public synchronized boolean autoRDT(){
         RDTForced=false;
         return isRDT();
    }
     
     /**
     *  Enable auto RDT setting new state
     * @return
     */
    public synchronized boolean autoRDT(boolean enabled){
         RDTForced=false;
         if (enabled) enableRDT();
         else disableRDT();
         return isRDT();
    }
     /**
     *  Enable RDT if is not force to manal
     * @return
     */
    
     public synchronized boolean enableRDT(){
         if(!RDTForced)
            packetizer.enableRDT();
         return isRDT();
    }
     
      /**
     *  Disable RDT if is not force to manal
     * @return
     */
     public synchronized boolean disableRDT(){
            if(!RDTForced)
                packetizer.disableRDT();
            return isRDT();
     }
     
     /**
      *  
      * @return current RDT status
      */
     public synchronized boolean isRDT(){
         return packetizer.isRDT();
    }
   

}
