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
            s = new VoiceSessionSettings(2, 2, "192.168.0.3");
            
            /*
             * ATTENZIONE! per l'esecuzione su LAN decommentare le prossime 3 istruzioni, commentare la quarta
             * ed aprire le porte del firewall (da 8765 a 8770 su un client e da 8865 a 8870 sull'altro e cambiare i parametri delle due istruzioni seguenti)
             */
            s.setRemote(s.getReceiveFormatCode(), s.getRemoteAddress(), 8866, 8867, 8869); //.., RTP, RTCP, RECOVERY.
            s.setLocal(s.getSendFormatCode(), 8766, 8767, 8769); //.., RTP, RTCP, RECOVERY.
            ss = new VoiceSession(s, true, 8769, 8869, 0); //PER IL RECOVERY: .., local, remote, 0.
            //ss = new VoiceSession(s, true, 0, 0, 0);
            
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
