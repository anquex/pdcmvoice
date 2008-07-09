/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.impl;

import javax.sound.sampled.AudioInputStream;
import jlibrtp.RTPSession;

import pdcmvoice.*;
import javax.sound.sampled.*;

/**
 *
 * @author marco
 */
public class VoiceSessionSender extends Thread{
    
    private Encoder encoder;
    private RTPSession rtpsession;
    private Packetizer packetizer;
    private AudioCapture capture;
    private int formatCode;
    
    public VoiceSessionSender(int formatCode, RTPSession rtp){
        this.formatCode=formatCode;
        this.rtpsession=rtp;
        capture= new AudioCapture(formatCode, //fortmato in cui codificare
                                  null, //default mixer
                                  50);  //
        packetizer= new Packetizer(rtp);
        try{
        capture.open();
        }catch(Exception e){e.printStackTrace();}
        AudioInputStream ais=capture.getAudioInputStream();
        encoder=new Encoder(formatCode, ais);
        encoder.registerPacketizer(packetizer);     
    }

    
    /* Acquire audio, encode and send
     * 
     * 
     */
    
    public void run(){
        try {
            
            capture.start();
            encoder.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    
    }
   

}