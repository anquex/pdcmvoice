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
            s = new VoiceSessionSettings(2, 2, "10.0.0.100");
            ss = new VoiceSession(s, true);
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