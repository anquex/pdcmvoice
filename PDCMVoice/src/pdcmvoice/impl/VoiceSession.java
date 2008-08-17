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

import java.util.Enumeration;
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

    private long startTimestamp;

    private final boolean DEBUG=true;
    private boolean listeningStarted;
    private boolean transmittingStarted;

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


            //RECOVERY COLLETION FROM SETTINGS
            withRecovery=settings.withRecovery();

            if (withRecovery)
            {

                
                Socket client = null;
                Socket server = null;

                try {
                    ServerSocket serverSocket = new ServerSocket(settings.getLocalRecoveryPort());
                    client = new Socket(settings.getRemoteAddress(), settings.getRemoteRecoveryPort());
                    server = serverSocket.accept();
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                RecoveryCollection localCollection = new RecoveryCollection("local", 0, settings.getSendFormatCode(), RECOVERY_LOCAL_COLLECTION_DEBUG);
                RecoveryCollection remoteCollection = new RecoveryCollection("remote", 0, settings.getReceiveFormatCode(), RECOVERY_REMOTE_COLLECTION_DEBUG);

                RecoveryConnection recoveryConnection = new RecoveryConnection(server, localCollection, client, remoteCollection, rtpSession, RECOVERY_CONNECTION_DEBUG);

                rs = new RecoveryServerThread(recoveryConnection, this);
                rc = new RecoveryClientThread(recoveryConnection, this);

                senderSession= new VoiceSessionSender(
                                                        settings.getSendFormatCode(),
                                                        rtpSession,
                                                        localCollection 
                                                        );
                receiverSession=new VoiceSessionReceiver(
                                                       settings.getReceiveFormatCode(),
                                                       rtpSession,
                                                       remoteCollection);

             
                
//                settings.getRemoteRecoveryPort(); 
//                settings.getLocalRecoveryPort();

            }
            
            vsc=new VoiceSessionController(this);
            rtpSession.naivePktReception(true);
            rtpSession.addParticipant(settings.getPartecipant());

            setMinorSettings();


    }

    public VoiceSession (VoiceSessionSettings settings, boolean withRecovery, int localPort, int remotePort, int encodedPacketSize) throws SocketException{

        this.settings=settings;

         rtpSocket = new DatagramSocket(settings.getLocalRTPPort());
         rtcpSocket = new DatagramSocket(settings.getLocalRTCPPort());

        rtpSession = new RTPSession(rtpSocket, rtcpSocket);

        Socket client = null;
        Socket server = null;

        if (localPort <= 0)
            localPort = DEFAULT_RECOVERY_PORT_LOCAL;
        if (remotePort <= 0)
            remotePort = DEFAULT_RECOVERY_PORT_LOCAL;
        if (encodedPacketSize <= 0)
            encodedPacketSize = DEFAULT_ENCODED_PACKET_SIZE;

        try {
            ServerSocket serverSocket = new ServerSocket(localPort);
            client = new Socket(settings.getRemoteAddress(), remotePort);
            server = serverSocket.accept();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        RecoveryCollection localCollection = new RecoveryCollection("local", encodedPacketSize, 1, RECOVERY_LOCAL_COLLECTION_DEBUG);
        RecoveryCollection remoteCollection = new RecoveryCollection("remote", encodedPacketSize, 1, RECOVERY_REMOTE_COLLECTION_DEBUG);

        RecoveryConnection recoveryConnection = new RecoveryConnection(server, localCollection, client, remoteCollection, rtpSession, RECOVERY_CONNECTION_DEBUG);

        rs = new RecoveryServerThread(recoveryConnection, this);
        rc = new RecoveryClientThread(recoveryConnection, this);

        senderSession= new VoiceSessionSender(
                                                settings.getSendFormatCode(),
                                                rtpSession,
                                                localCollection
                                                );
        receiverSession=new VoiceSessionReceiver(
                                               settings.getReceiveFormatCode(),
                                               rtpSession,
                                               remoteCollection);
        vsc=new VoiceSessionController(this);
        rtpSession.naivePktReception(true);
        rtpSession.addParticipant(settings.getPartecipant());

        this.withRecovery = withRecovery;
        setMinorSettings();


}

    public void start() throws UnsupportedAudioFileException, Exception{
            startListening();
            starTransmitting();
            if (withRecovery)
            {
                rs.start();
                rc.start();
            }

    }

    public void stop(){
        if(listeningStarted||transmittingStarted)
            rtpSession.endSession();
        if(transmittingStarted){
            senderSession.stop();
            out("Receiving Stopped");
        }
        if(listeningStarted){
            receiverSession.stop();
            out("Sending Stopped");
        }

        // recovery connection should still be running
        if (withRecovery)
        {
            rc.endOfStream = true;
            
            while (rc.endOfStream)
            {
                //if (rc.getRecConn().debug)
                    System.out.println("--VOICE SESSION-- ATTESA DI RecoveryClientThread...");
                
                try {
                    Thread.sleep(500); //attesa durante la ricezione
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
            
            
            
            
        }
        //closeSockets(); -> rc...
        out ("Voice Session Stopped");


    }
    public int setMinBufferedMillis(int n){
        return receiverSession.getDepacketizer()
                .getPlayoutBuffer().setMinBufferedMillis(n);
    }

    public int getMinBufferedMillis(){
        return receiverSession.getDepacketizer()
                .getPlayoutBuffer().getMinBufferedMillis();
    }

    public int setMaxBufferedMillis(int n){
          return receiverSession.getDepacketizer().
                  getPlayoutBuffer().setMaxBufferedMillis(n);
    }
    public int getMaxBufferedMillis(){
        return receiverSession.getDepacketizer()
                .getPlayoutBuffer().getMaxBufferedMillis();
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

    private void closeSockets(){
        rtpSocket.close();
        rtcpSocket.close();
    }

    public RTCPStats getRTCPStats(){
        return vsc.getRTCPStats();
    }

    public String toString(){
        String out="";
        out+="---- VOICE SESSION DESCRIPTION";
        out+="\n---------- Send format code    : "+settings.getSendFormatCode();
        out+="\n---------- Receiver format code: "+settings.getReceiveFormatCode();
        out+="\n---------- Speex Quality       : "+settings.getLocalSpeexQuality();
        out+="\n---------- Local RTP  port     : "+settings.getLocalRTPPort();
        out+="\n---------- Local RTCP port     : "+settings.getLocalRTCPPort();
        out+="\n---------- Local Recovery  port: "+settings.getLocalRecoveryPort();
        out+="\n---------- Remote address      : "+settings.getRemoteAddress();
        out+="\n---------- Remote RTP Port     : "+settings.getRemoteRTPPort();
        out+="\n---------- Remote RTCP Port    : "+settings.getRemoteRTCPPort();
        out+="\n---------- Remote Recovery Port: "+settings.getRemoteRecoveryPort();
        return out;
    }

    // non avevo voglia di modificare i costruttori per cui c'è questa funzione
    // che imposta i parametri minori (es frames per pachetto) presi dalla gui
    private void setMinorSettings(){

        setSpeexQuality(settings.getLocalSpeexQuality());
        //setFramesPerPacket(1);
        setFramesPerPacket(settings.framesPerPacket());
        setMaxBufferedMillis(settings.getMaxBufferSize());
        setMinBufferedMillis(settings.getMinBufferSize());
        dynamic(settings.isDynamic());
        RDT(settings.isRDT());
        //RDT(true);
        //out (""+settings.framesPerPacket()+" "+settings.isRDT());
    }

    public void RDT(boolean enabled){
        Packetizer p=senderSession.getPacketizer();
        if(enabled)
            p.enableRDT();
        else
            p.disableRDT();
    }
    public boolean isRDT(){
        Packetizer p=senderSession.getPacketizer();
        return p.isRDT();
    }


    public void setSpeexQuality(int n){
        Encoder e=senderSession.getEncoder();
        e.setSpeexQuality(n);
    }

    public void dynamic(boolean enabled){
        if(vsc!=null){
            if(enabled)
               vsc.continueOptimizing();
            else vsc.pauseOptimizing();
        }
    }
    public boolean isDynamic(){
        return !vsc.isPaused();
    }

    public long lastReceivedPacketSN(){
        return receiverSession.getDepacketizer().lastPacketSN();
    }
    public boolean lastReceivedPacketRDT(){
        return receiverSession.getDepacketizer().lastPacketRDT();
    }
    public int lastReceivedPacketFrames(){
        return receiverSession.getDepacketizer().lastPacketFrames();
    }
    public int lastReceivedPacketFramesSize(){
        return receiverSession.getDepacketizer().lastPacketFramesSize();
    }

    public int lastReceivedPacketPayload(){
        return receiverSession.getDepacketizer().lastPacketPayload();
    }

    public long lastSentPacketSN(){
        return senderSession.getPacketizer().lastPacketSN();

    }
    public boolean lastSentPacketRDT(){
        return senderSession.getPacketizer().lastPacketRDT();
    }
    public int lastSentPacketFrames(){
        return senderSession.getPacketizer().lastPacketFrames();
    }
    public int lastSentPacketFramesSize(){
        return senderSession.getPacketizer().lastPacketFramesSize();
    }

    public int lastSentPacketPayload(){
        return senderSession.getPacketizer().lastPacketPayload();
    }

    public int lastEncodedFrameSize(){
        // could return 0 if no frame produced
        return senderSession.getEncoder().getLastFrameSize();
    }

    public int getBufferedMillis(){
        return receiverSession.getDepacketizer().
                getPlayoutBuffer().getBufferedMillis();
    }

    public float getPercivedSessionPLoss(){
            return receiverSession.getDepacketizer().getPlayoutBuffer().sessionPloss();
    }
    public float getPercivedIntervalPLoss(){
            return receiverSession.getDepacketizer().getPlayoutBuffer().intervalPloss();
    }

    public void updateSessionPercivedPloss(){
        receiverSession.getDepacketizer().getPlayoutBuffer().updateIntervalPloss();
        receiverSession.getDepacketizer().getPlayoutBuffer().updateSessionPloss();

    }

    public long getSessionDurationMillis(){
        return System.currentTimeMillis()-startTimestamp;
    }

    public String getElapsed(){
        long t=getSessionDurationMillis();
        int minutes;
        minutes=(int) (t / (1000 * 60));
        String seconds=""+(int) ((t - (minutes * 60 * 1000)) / 1000);
        if (seconds.length()==1) seconds="0"+seconds;
        return ""+minutes+":"+seconds;
    }

    public int myJitter(){
        Enumeration<Participant> participants = rtpSession.getParticipants();
        while(participants.hasMoreElements()){
            return participants.nextElement().interArvJitter();
        }
        return 0;
    }

    public int myFractionLoss(){
        Enumeration<Participant> participants = rtpSession.getParticipants();
        while(participants.hasMoreElements()){
            return participants.nextElement().myFractionLoss();
        }
        return 0;
    }

    public int avgJitterSeen(){
        return vsc.getRTCPStats().getAverageJitter();
    }

    public int avgFractionLossSeen(){
        return vsc.getRTCPStats().getAveragePloss();
    }

    public void startListening() throws UnsupportedAudioFileException, Exception{
        receiverSession.init();
        rtpSession.registerRTPSession(receiverSession.getDepacketizer(),
                          vsc, //to be implemented
                          null);
        receiverSession.start();
        startTimestamp=System.currentTimeMillis();
        out ("Voice Session Started Listening");
        if (DEBUG) out(toString());
        listeningStarted=true;
    }
    public void starTransmitting() throws Exception{
        if(vsc!=null)
            vsc.start();
        senderSession.start();
        out ("Voice Session Started Transmitting");
        if (DEBUG) out(toString());
        transmittingStarted=true;
    }

    public boolean isActive(){
        //TO DO
        return transmittingStarted && listeningStarted;
    }

}// END VOICE SESSION
