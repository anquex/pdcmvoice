/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.impl;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.sound.sampled.UnsupportedAudioFileException;
import jlibrtp.Participant;
import pdcmvoice.settings.VoiceSessionSettings;
import jlibrtp.RTPSession;
import static pdcmvoice.impl.Constants.*;
//RECOVERY
import java.net.Socket;
import java.net.ServerSocket;
import pdcmvoice.recovery.*; 

/**
 *
 * @author marco
 */
public class VoiceSession {

    private VoiceSessionSender senderSession;
    private VoiceSessionReceiver receiverSession;
    private VoiceSessionController vsc;
    private RTPSession rtpSession;
    private VoiceSessionSettings settings;
    
    //RECOVERY
    private RecoveryServerThread rs;
    private RecoveryClientThread rc;
    boolean withRecovery;

    DatagramSocket rtpSocket = null;
    DatagramSocket rtcpSocket = null;
    DatagramSocket recoverySocket = null; // still not used

    public VoiceSession (VoiceSessionSettings settings) throws SocketException{

            this.settings=settings;

             rtpSocket = new DatagramSocket(settings.getLocalRTPPort());
             rtcpSocket = new DatagramSocket(settings.getLocalRTCPPort());

            rtpSession = new RTPSession(rtpSocket, rtcpSocket);

            senderSession= new VoiceSessionSender(
                                                    settings.getSendFormatCode(),
                                                    rtpSession);
            receiverSession=new VoiceSessionReceiver(
                                                   settings.getReceiveFormatCode(),
                                                   rtpSession);
            //rtpSession.naivePktReception(true);
            rtpSession.addParticipant(settings.getPartecipant());
            withRecovery = false;


    }
    
    public VoiceSession (VoiceSessionSettings settings, boolean withRecovery) throws SocketException{

        this.settings=settings;

         rtpSocket = new DatagramSocket(settings.getLocalRTPPort());
         rtcpSocket = new DatagramSocket(settings.getLocalRTCPPort());

        rtpSession = new RTPSession(rtpSocket, rtcpSocket);
        
        Socket client = null;
        Socket server = null;
        
        try {
            ServerSocket serverSocket = new ServerSocket(DEFAULT_RECOVERY_PORT_LOCAL);
            client = new Socket(InetAddress.getByName(settings.getRemoteAddress()), DEFAULT_RECOVERY_PORT_LOCAL);
            server = serverSocket.accept();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        RecoveryCollection localCollection = new RecoveryCollection("local", DEFAULT_ENCODED_PACKET_SIZE, 1, true);
        RecoveryCollection remoteCollection = new RecoveryCollection("remote", DEFAULT_ENCODED_PACKET_SIZE, 1, true);
         
        RecoveryConnection recoveryConnection = new RecoveryConnection(server, localCollection, client, remoteCollection, rtpSession, true);
        
        rs = new RecoveryServerThread(recoveryConnection);
        rc = new RecoveryClientThread(recoveryConnection);

        senderSession= new VoiceSessionSender(
                                                settings.getSendFormatCode(),
                                                rtpSession,
                                                localCollection
                                                );
        receiverSession=new VoiceSessionReceiver(
                                               settings.getReceiveFormatCode(),
                                               rtpSession,
                                               remoteCollection);
        //rtpSession.naivePktReception(true);
        rtpSession.addParticipant(settings.getPartecipant());
        
        this.withRecovery = withRecovery;


}

    public void start() throws UnsupportedAudioFileException, Exception{
            receiverSession.init();
            rtpSession.RTPSessionRegister(receiverSession.getDepacketizer(),
                              vsc, //to be implemented
                              null);
            receiverSession.start();
            senderSession.start();
            
            if (withRecovery)
            {
                rs.start();
                rc.start();
            }
            out ("Voice Session Started");

    }

    public void stop(){
            //rtpSession.endSession();
            senderSession.stop();
            receiverSession.stop();
            // recovery connection should still be running
            out ("Voice Session Stopped");

    }
    public int setMinBufferedMillis(int n){
        return receiverSession.getDepacketizer()
                .getPlayoutBuffer().setMinBufferedMillis(n);
    }

    public int getMinBufferedMillis(int n){
        return receiverSession.getDepacketizer()
                .getPlayoutBuffer().getMinBufferedMillis();
    }

    public int setMaxBufferedMillis(int n){
        return 0;
    }

    public int getBufferedFrames(){
        return receiverSession.getDepacketizer().getPlayoutBuffer().size();
    }

    public int getFramesPerPacket(){
        return senderSession.getPacketizer().framesPerPackets();
    }

    public int setFramesPerPacket(int n){
        return senderSession.getPacketizer().framesPerPackets(n);
    }

    public String getSendingFormatName(){
        return FORMAT_NAMES[settings.getSendFormatCode()-1];
    }


}
