/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.impl;

import javax.sound.sampled.UnsupportedAudioFileException;
import jlibrtp.*;
/**
 *
 * @author marco
 */
public class VoiceSessionReceiver extends Thread{
    
    final boolean DEBUG=true;
    
    private Decoder decoder;
    private AudioPlayback player;
    private RTPSession rtpsession;
    private Depacketizer depacketizer;
    private int formatCode;
    
    public VoiceSessionReceiver (int formatCode, RTPSession rtp){
        this.formatCode=formatCode;
        this.rtpsession=rtp;
        depacketizer= new Depacketizer(rtp);
        decoder=new Decoder(formatCode);
        player= new AudioPlayback(formatCode, //fortmato in cui codificare
                          null, //default mixer
                          60   //buffer size
                            );  //input queue
    }
    
    public void init() throws UnsupportedAudioFileException{
        depacketizer.registerDecoder(decoder);
        decoder.init();
        player.setAudioInputStream(decoder.getAudioInputStream());
        
        try{
            player.open();
        }catch(Exception e){e.printStackTrace();}
    }
    public void run() {
        try {
            player.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        depacketizer.init();
//        while(true){
//            System.out.println("Coda: "+playFramQueue.size());
//            try{
//                sleep(200);
//            }catch(InterruptedException e){};
//        }
        
    }

}
