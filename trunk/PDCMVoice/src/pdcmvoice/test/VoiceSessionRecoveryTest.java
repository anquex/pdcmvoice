/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.test;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.UnsupportedAudioFileException;
import pdcmvoice.*;
import pdcmvoice.impl.VoiceSession;
import pdcmvoice.settings.VoiceSessionSettings;

/**
 *
 * @author marco
 */
public class VoiceSessionRecoveryTest extends Thread{

    VoiceSessionSettings s;
    VoiceSession ss;

    public VoiceSessionRecoveryTest() {
        try {
            s = new VoiceSessionSettings(2, 2, "192.168.0.6");
            ss = new VoiceSession(s, true, 0, 0);
            
            /*
             * ATTENZIONE!! DISABILITARE LA PROSSIMA ISTRUZIONE (in Depacketizer.java) 
             * 
             * rtpSession.registerRTPSession(this, null, null); (vecchia rtpSession.RTPSessionRegister(this, null, null);)
             */
            
            ss.start();

        } catch (UnsupportedAudioFileException ex) {
            Logger.getLogger(VoiceSessionRecoveryTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(VoiceSessionRecoveryTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String[] args){
            VoiceSessionRecoveryTest v=new VoiceSessionRecoveryTest();

    }

}
