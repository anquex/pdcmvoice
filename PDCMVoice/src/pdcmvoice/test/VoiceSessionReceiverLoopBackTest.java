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

import org.xiph.speex.NbEncoder;

import jlibrtp.Participant;
import jlibrtp.RTPSession;
import pdcmvoice.impl.VoiceSessionReceiver;
//import pdcmvoice.recovery.RecoveryClientThread;
//import pdcmvoice.recovery.RecoveryCollection;
//import pdcmvoice.recovery.RecoveryConnection;
//import pdcmvoice.recovery.RecoveryServerThread;
import static pdcmvoice.impl.Constants.*;


import java.net.ServerSocket;


public class VoiceSessionReceiverLoopBackTest {
    
    public static void main (String[] args) throws Exception{
            DatagramSocket rtpSocket = null;
            DatagramSocket rtcpSocket = null;
            try {
                rtpSocket = new DatagramSocket(DEFAULT_RTP_PORT);
                rtcpSocket = new DatagramSocket(DEFAULT_RTCP_PORT);
//                rtpSocket = new DatagramSocket(9000);
//                rtcpSocket = new DatagramSocket(9001);
            } catch (SocketException e) {
                e.printStackTrace();
            }
            RTPSession rtpsession = new RTPSession(rtpSocket, rtcpSocket);
            Participant p = new Participant("127.0.0.1", 7000, 7100); //RTCP Port
            //rtpsession.addParticipant(p);
            rtpsession.naivePktReception(true);
            VoiceSessionReceiver r = new VoiceSessionReceiver(1, rtpsession);
            rtpsession.registerRTPSession(r.getDepacketizer(),
            null, //to be implemented
            null);
            r.init();
            r.start();
            
          //RECOVERY
            /*
            ServerSocket server = new ServerSocket(6001);
            Socket serverSocket = server.accept();
            
            Socket client = new Socket("localhost", 6000);
            
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
