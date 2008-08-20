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

    /*
     * OPERAZIONI PRELLIMINARI 
     * - inserire IP in "s = new VoiceSessionSettings(2, 2, "xxx.xxx.xxx.xxx");"
     * - verificare il percorso per il file mixed.wav, RecoveryClientThread.java, intorno alla riga 711.
     */
    
    public VoiceSessionRecoveryTest() {
        try {
            s = new VoiceSessionSettings(2, 2, "192.168.0.3");
            s.withRecovery(true);
            //s.setRemote(s.getReceiveFormatCode(), s.getRemoteAddress(), 8866, 8867, 8869); //.., RTP, RTCP, RECOVERY.
            //s.setLocal(s.getSendFormatCode(), 8766, 8767, 8769); //.., RTP, RTCP, RECOVERY.
            
            /*
             * ATTENZIONE! A volte la prossima istruzione comporta l'impossibilità di inizializzare la linea del microfono.
             * In tal caso usare l'istruzione successiva, modificando, se serve, le porte per il recovery
             */
            ss = new VoiceSession(s);
            //ss = new VoiceSession(s, true, 0, 0, 0); //PER IL RECOVERY: .., local, remote, 0.
            
            
            //PROVA
            //ss = new VoiceSession(s, true, 8769, 8869, 0); //PER IL RECOVERY: .., local, remote, 0.
            
           
            
                    
            /*
             * ATTENZIONE!! DISABILITARE LA PROSSIMA ISTRUZIONE (in Depacketizer.java) 
             * 
             * rtpSession.registerRTPSession(this, null, null); (vecchia rtpSession.RTPSessionRegister(this, null, null);)
             */ 
            
            ss.start();
            Thread.sleep(15000);
            ss.stop();

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
