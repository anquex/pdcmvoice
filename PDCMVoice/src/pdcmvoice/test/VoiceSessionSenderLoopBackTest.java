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
import java.util.logging.Level;
import java.util.logging.Logger;
import jlibrtp.Participant;
import jlibrtp.RTPSession;
import pdcmvoice.impl.VoiceSessionSender;
import static pdcmvoice.impl.Constants.*;

import pdcmvoice.recovery.*; 


public class VoiceSessionSenderLoopBackTest extends Thread{

    public void run() {
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
            //Participant p = new Participant("10.0.0.100", DEFAULT_RTP_PORT, DEFAULT_RTCP_PORT); //RTCP Port
            Participant p = new Participant("127.0.0.1", 9000, 9001); //RTCP Port
            rtpsession.addParticipant(p);
            VoiceSessionSender s = new VoiceSessionSender(2, rtpsession);
            //s.getPacketizer().framesPerPackets(2);
            s.getPacketizer().enableRDT();
            s.start();
            sleep(10000);
            s.getPacketizer().disableRDT();
        } catch (Exception ex) {
            Logger.getLogger(VoiceSessionSenderLoopBackTest.class.getName()).log(Level.SEVERE, null, ex);
        }
            
    }
    
    
    
    public static void main (String[] args) throws Exception{
        VoiceSessionSenderLoopBackTest t= new VoiceSessionSenderLoopBackTest();
        t.start();
        

            
          //RECOVERY
            /*
            Socket client = new Socket(InetAddress.getLocalHost(), 6001);
            
            ServerSocket server = new ServerSocket(6000);
            Socket serverSocket = server.accept();
            
            
            NbEncoder encoder = new NbEncoder();
            int pktSize = encoder.getFrameSize();
            RecoveryCollection localCollection = new RecoveryCollection("local", pktSize, 1);
            RecoveryCollection remoteCollection = new RecoveryCollection("remote", pktSize, 1);
            
            RecoveryConnection recoveryConnection = new RecoveryConnection(serverSocket, localCollection, client, remoteCollection, rtpsession, false);
            
            RecoveryServerThread rs = new RecoveryServerThread(recoveryConnection);
            RecoveryClientThread rc = new RecoveryClientThread(recoveryConnection, rs);
            rs.start();
            rc.start();
            */
    }
    

}
