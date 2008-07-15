/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.test;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jlibrtp.Participant;
import jlibrtp.RTPSession;
import pdcmvoice.impl.Packetizer;
import pdcmvoice.impl.VoiceSessionSender;
import static pdcmvoice.impl.Constants.*;

/**
 *
 * @author marco
 */
public class PacketizerTest extends Thread{

    public void run(){
        try {
            DatagramSocket rtpSocket = null;
            DatagramSocket rtcpSocket = null;
            try {
                rtpSocket = new DatagramSocket(7000);
                rtcpSocket = new DatagramSocket(7100);
            } catch (SocketException e) {
                e.printStackTrace();
            }
            RTPSession rtpsession = new RTPSession(rtpSocket, rtcpSocket);
            Participant p = new Participant("192.168.0.1", 9000, 9001); //RTCP Port
            rtpsession.addParticipant(p);
            rtpsession.payloadType(PAYLOAD_SPEEX);
            VoiceSessionSender s = new VoiceSessionSender(1, rtpsession);

            Packetizer packetizer = s.getPacketizer();
            packetizer.framesPerPackets(2);
            s.start();
            while (true){
                sleep(50);
                packetizer.framesPerPackets(1);
                sleep(50);
                packetizer.framesPerPackets(2);
            }
            
        } catch (InterruptedException ex) {
            Logger.getLogger(PacketizerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main (String[] args){
        PacketizerTest t= new PacketizerTest();
        t.start();
    }

}
