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
public class VoiceSessionTest extends Thread{

    VoiceSessionSettings s;
    VoiceSession ss;

    public VoiceSessionTest() {
        try {
            s = new VoiceSessionSettings(2, 2, "192.168.0.4");
            ss = new VoiceSession(s);
            ss.start();

        } catch (UnsupportedAudioFileException ex) {
            Logger.getLogger(VoiceSessionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(VoiceSessionTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String[] args){
            VoiceSessionTest v=new VoiceSessionTest();

    }

}
