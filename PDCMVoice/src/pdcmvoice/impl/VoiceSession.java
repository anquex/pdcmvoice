/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.impl;

import java.net.DatagramSocket;
import java.net.SocketException;
import javax.sound.sampled.UnsupportedAudioFileException;
import jlibrtp.Participant;
import pdcmvoice.settings.VoiceSessionSettings;
import jlibrtp.RTPSession;
import static pdcmvoice.impl.Constants.*;

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


    }

    public void start() throws UnsupportedAudioFileException, Exception{
            receiverSession.init();
            rtpSession.RTPSessionRegister(receiverSession.getPacketizer(),
                              vsc, //to be implemented
                              null);
            receiverSession.start();
            senderSession.start();

    }

    public void stop(){
            rtpSession.endSession();
            senderSession.stop();
            receiverSession.stop();
            // recovery connection should still be running

    }


}
