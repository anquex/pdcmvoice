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

import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import pdcmvoice.recovery.*; 

/*
 * Avviare prima VoiceSessionReceiverLoopBackTest e poi VoiceSessionSenderLoopBackTest
 */
public class RecoveryVoiceSessionSenderLoopBackTest {
    
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

            //rtpsession.payloadType(FORMAT_CODE_SPEEX_NB);

            rtpsession.payloadType(PAYLOAD_SPEEX);
            
//            VoiceSessionSender s = new VoiceSessionSender(2, rtpsession);
//            s.start();

            
            
          //RECOVERY
            
            Socket client = new Socket(InetAddress.getLocalHost(), 6001);
            
            ServerSocket server = new ServerSocket(6000);
            Socket serverSocket = server.accept();
            
            
//            NbEncoder encoder = new NbEncoder();
//            int pktSize = encoder.getFrameSize();
              int pktSize = 20;
            
            RecoveryCollection localCollection = new RecoveryCollection("local", pktSize, 1, true);
            RecoveryCollection remoteCollection = new RecoveryCollection("remote", pktSize, 1, true);
            
          //c'è una RecoveryConnection per VoiceSessionReceiverLoopBackTest ed una per VoiceSessionSenderLoopBackTest anche se entrambe sono tra gli stessi endpoint
            //teoricamente la RecoveryConnection deve essere unica! infatti in questo file la remoteConnection rimane inutilizzata!
            //EVENTUALE SOLUZIONE: cambiare il costruttore in modo che crei solo la collezione che serve davvero
// DA FARE //EVENTUALE SOLUZIONE 2: creare i due thread di recovery e i due thread VoiceSessionReceiver e VoiceSessionSender all'interno dello stesso main in modo da creare solo le due collezioni che servono ed un0unica RecoveryConnection. E' necessario per le operazioni di salvataggio eseguite dal ClientThread
            RecoveryConnection recoveryConnection = new RecoveryConnection(serverSocket, localCollection, client, remoteCollection, rtpsession, true);
            
//            RecoveryServerThread rs = new RecoveryServerThread(recoveryConnection);
//            //RecoveryClientThread rc = new RecoveryClientThread(recoveryConnection, rs);
//            rs.start();
//            //rc.start();
            
          //END RECOVERY
            
            VoiceSessionSender s = new VoiceSessionSender(1, rtpsession, localCollection); 
            s.start();
            
    }
    

}
