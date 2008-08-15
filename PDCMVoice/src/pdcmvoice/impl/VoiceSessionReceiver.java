/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.impl;

import jlibrtp.RTPSession;
import javax.sound.sampled.UnsupportedAudioFileException;
import jlibrtp.*;
import pdcmvoice.recovery.RecoveryCollection;
/**
 *
 * @author marco
 */
public class VoiceSessionReceiver {

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
                          100   //buffer size
                            );  //input queue
    }

    public VoiceSessionReceiver (int formatCode, RTPSession rtp, RecoveryCollection remote){
        this(formatCode,rtp);
        depacketizer= new Depacketizer(rtp, remote);
        decoder=new Decoder(formatCode, true);
    }

    public void init() throws UnsupportedAudioFileException{
        depacketizer.registerDecoder(decoder);
        decoder.init();
        player.setAudioInputStream(decoder.getAudioInputStream());

        try{
            player.open();
        }catch(Exception e){e.printStackTrace();}
    }

    public void start() throws Exception {
            player.start();
            depacketizer.init();
            // after depacketizer is inited I start receiving
            // rtp packets

    }

    public void stop(){
        depacketizer.terminate();
        player.close();
    }

    public Depacketizer getDepacketizer(){
        return depacketizer;
    }
}
