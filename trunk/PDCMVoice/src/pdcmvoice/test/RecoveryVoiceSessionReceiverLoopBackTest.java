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
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import org.xiph.speex.NbEncoder;

import jlibrtp.Participant;
import jlibrtp.RTPSession;
import pdcmvoice.impl.VoiceSessionReceiver;
import pdcmvoice.recovery.RecoveryClientThread;
import pdcmvoice.recovery.RecoveryCollection;
import pdcmvoice.recovery.RecoveryConnection;
import pdcmvoice.recovery.RecoveryServerThread;
import static pdcmvoice.impl.Constants.*;


import java.net.ServerSocket;

/*
 * Avviare prima VoiceSessionReceiverLoopBackTest e poi VoiceSessionSenderLoopBackTest
 */
public class RecoveryVoiceSessionReceiverLoopBackTest {
    
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
            //rtpsession.addParticipant(p);
            rtpsession.naivePktReception(true);

            //rtpsession.payloadType(FORMAT_CODE_SPEEX_NB);

            rtpsession.payloadType(PAYLOAD_SPEEX);
            
            
//            VoiceSessionReceiver r = new VoiceSessionReceiver(2, rtpsession);
//            r.init();
//            r.start();

            
            
          //RECOVERY
            
            ServerSocket server = new ServerSocket(6001);
            Socket serverSocket = server.accept();
            
            Socket client = new Socket("localhost", 6000);
            
//            NbEncoder encoder = new NbEncoder();
//            int pktSize = encoder.getFrameSize();
            int pktSize = 20;
            RecoveryCollection localCollection = new RecoveryCollection("local", pktSize, 1, true);
            RecoveryCollection remoteCollection = new RecoveryCollection("remote", pktSize, 1, true);
            
            //c'è una RecoveryConnection per VoiceSessionReceiverLoopBackTest ed una per VoiceSessionSenderLoopBackTest anche se entrambe sono tra gli stessi endpoint
            //teoricamente la RecoveryConnection deve essere unica! infatti in questo file la localConnection rimane inutilizzata!
            //EVENTUALE SOLUZIONE: cambiare il costruttore in modo che crei solo la collezione che serve davvero
            //EVENTUALE SOLUZIONE 2: creare i due thread di recovery e i due thread VoiceSessionReceiver e VoiceSessionSender all'interno dello stesso main in modo da creare solo le due collezioni che servono ed un'unica RecoveryConnection
            RecoveryConnection recoveryConnection = new RecoveryConnection(serverSocket, localCollection, client, remoteCollection, rtpsession, false);
            
            //RecoveryServerThread rs = new RecoveryServerThread(recoveryConnection);
            RecoveryClientThread rc = new RecoveryClientThread(recoveryConnection);
            //rs.start();
            rc.start();
            
            VoiceSessionReceiver r = new VoiceSessionReceiver(1, rtpsession, remoteCollection);
            r.init();
            r.start();
            
    }
    

}
