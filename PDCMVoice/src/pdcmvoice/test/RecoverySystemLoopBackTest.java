package pdcmvoice.test;

import java.net.DatagramSocket;
import java.net.SocketException;
import jlibrtp.Participant;
import jlibrtp.RTPSession;
import pdcmvoice.impl.VoiceSessionSender;
import pdcmvoice.impl.VoiceSessionReceiver;
import static pdcmvoice.impl.Constants.*;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import pdcmvoice.recovery.*; 

public class RecoverySystemLoopBackTest {
    
    public static void main (String[] args) throws Exception{
        
        /*
         * RTP RECEIVER
         */
        DatagramSocket rtpSocketR = null;
        DatagramSocket rtcpSocketR = null;
        try {
            rtpSocketR = new DatagramSocket(9000);
            rtcpSocketR = new DatagramSocket(9001);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        RTPSession rtpsessionR = new RTPSession(rtpSocketR, rtcpSocketR);
        Participant pR = new Participant("127.0.0.1", 7000, 7100); //RTCP Port
        //rtpsession.addParticipant(p);
        rtpsessionR.naivePktReception(true);
    
        //FORMAT_CODE_SPEEX_WB
        //FORMAT_CODE_SPEEX_NB
        //FORMAT_CODE_iLBC
        rtpsessionR.payloadType(FORMAT_CODE_SPEEX_WB);
    
        //rtpsessionR.payloadType(PAYLOAD_SPEEX);
        
        /*
         * RTP SENDER
         */
        
        DatagramSocket rtpSocketS = null;
        DatagramSocket rtcpSocketS = null;
        try {
            rtpSocketS = new DatagramSocket(7000);
            rtcpSocketS = new DatagramSocket(7100);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        RTPSession rtpsessionS = new RTPSession(rtpSocketS, rtcpSocketS);
        Participant pS = new Participant("127.0.0.1", 9000, 9001); //RTCP Port
        rtpsessionS.addParticipant(pS);
    
        //FORMAT_CODE_SPEEX_WB
        //FORMAT_CODE_SPEEX_NB
        //FORMAT_CODE_iLBC
        rtpsessionS.payloadType(FORMAT_CODE_SPEEX_WB);
    
        //rtpsessionS.payloadType(PAYLOAD_SPEEX);
        
        
        /*
         * RECOVERY
         */
        
        /*
         * Nel caso generico (non LoopBack) ciascuno dei due partecipanti disporr� di una sola recoveryConnection, di una localCollection e di una remoteCollection.
         * La connessione considerer� come "server" il localhost e come "client" l'altro partecipante. Il ClientThread legger�/scriver� sul "server" (socket) usando la remote mentre il ServerThread legger�/scriver� sul "client" (socket) usando la local.
         */
        
        //LOOPBACK
        //Instauro una sola RecoveryConnection tra B (sender) ed A (receiver) sulla porta 6001
        ServerSocket serverA = new ServerSocket(6001);
        
        Socket clientB = new Socket(InetAddress.getLocalHost(), 6001);  //sostituire InetAddress.getLocalHost() con l'ip dell'altro interlocutore
        
        Socket serverSocketA = serverA.accept();
        
        
        
        
//        
//        ServerSocket serverB = new ServerSocket(6000);                //Queste tre istruzioni verranno eseguite dall'altro interlocutore
//        Socket serverSocketB = serverB.accept();                      //Queste tre istruzioni verranno eseguite dall'altro interlocutore
//        
//        Socket clientA = new Socket("localhost", 6000);               //Queste tre istruzioni verranno eseguite dall'altro interlocutore
                                                                        //Sostituire "localhost" con l'ip di questo Interlocutore
        
        int pktSize = 20;
        RecoveryCollection localCollection = new RecoveryCollection("local", pktSize, 1, RECOVERY_LOCAL_COLLECTION_DEBUG);
        RecoveryCollection remoteCollection = new RecoveryCollection("remote", pktSize, 1, RECOVERY_REMOTE_COLLECTION_DEBUG);
         
        RecoveryConnection recoveryConnection = new RecoveryConnection(serverSocketA, localCollection, clientB, remoteCollection, rtpsessionR, RECOVERY_CONNECTION_DEBUG);
        
        RecoveryServerThread rs = new RecoveryServerThread(recoveryConnection, null);
        RecoveryClientThread rc = new RecoveryClientThread(recoveryConnection, null);
        rs.start();
        rc.start();
        
        /*
         * ATTENZIONE!! ABILITARE LA PROSSIMA ISTRUZIONE (in Depacketizer.java) SOLO 
         * PER L'ESECUZIONE DI RecoverySystemLoopBackTest.java
         * 
         * 
         * rtpSession.registerRTPSession(this, null, null); (vecchia rtpSession.RTPSessionRegister(this, null, null);)
         */
        
        
        
        VoiceSessionReceiver r = new VoiceSessionReceiver(1, rtpsessionR, remoteCollection);
        r.init();
        r.start();
        
        VoiceSessionSender s = new VoiceSessionSender(1, rtpsessionS, localCollection); 
        s.start();
    }

}
