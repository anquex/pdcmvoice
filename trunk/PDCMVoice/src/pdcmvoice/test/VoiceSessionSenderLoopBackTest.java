package pdcmvoice.test;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author marco
 */
import java.net.DatagramSocket;
import java.net.SocketException;
import jlibrtp.Participant;
import jlibrtp.RTPSession;
import pdcmvoice.impl.VoiceSessionSender;
import static pdcmvoice.impl.Constants.*;

public class VoiceSessionSenderLoopBackTest {
    
    public static void main (String[] args) throws Exception{
            DatagramSocket rtpSocket = null;
            DatagramSocket rtcpSocket = null;
            try {
                rtpSocket = new DatagramSocket(7000);
                rtcpSocket = new DatagramSocket(7100);
            } catch (SocketException e) {
                e.printStackTrace();
            }
            RTPSession rtpsession = new RTPSession(rtpSocket, rtcpSocket);
            Participant p = new Participant("127.0.0.1", 9000, 9001); //RTCP Port
            rtpsession.addParticipant(p);
            rtpsession.payloadType(PAYLOAD_iLBC_RDT);
            VoiceSessionSender s = new VoiceSessionSender(3, rtpsession);
            s.start();
    }
    

}
