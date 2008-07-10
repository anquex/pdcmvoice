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
import pdcmvoice.impl.VoiceSessionReceiver;
import static pdcmvoice.impl.Constants.*;

public class VoiceSessionReceiverLoopBackTest {
    
    public static void main (String[] args) throws Exception{
            DatagramSocket rtpSocket = null;
            DatagramSocket rtcpSocket = null;
            try {
                rtpSocket = new DatagramSocket(9000);
                rtcpSocket = new DatagramSocket(9001);
            } catch (SocketException e) {
                e.printStackTrace();
            }
            RTPSession rtpsession = new RTPSession(rtpSocket, rtcpSocket);
            Participant p = new Participant("127.0.0.1", 7000, 7100); //RTCP Port
            rtpsession.addParticipant(p);
            rtpsession.naivePktReception(true);
            rtpsession.payloadType(PAYLOAD_SPEEX_RDT);
            VoiceSessionReceiver r = new VoiceSessionReceiver(2, rtpsession);
            rtpsession.packetBufferBehavior(3);
            r.init();
            r.start();
    }
    

}
