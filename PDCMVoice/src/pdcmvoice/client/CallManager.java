/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.UnsupportedAudioFileException;
import pdcmvoice.impl.VoiceSession;
import pdcmvoice.settings.AudioSettings;
import pdcmvoice.settings.ConnectionSettings;
import pdcmvoice.settings.TransmissionSettings;
import pdcmvoice.settings.VoiceSessionSettings;
import static pdcmvoice.impl.Constants.*;

/**
 *
 * @author marco
 */

public class CallManager extends Thread{
    
    private Client client;
    private Socket socket;
    private int id;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isCaller;

    private boolean callActive;
    private boolean sentBye;
    private boolean sentAck;
    private boolean parametersSent;

    //Received Infos
    private String address;
    private String remoteUser;
    private int RTPPort;
    private int RTCPPort;
    private int RecoveryPort;
    private int RemoteEncoding;
    private boolean withRemoteBackground;

    AudioSettings remoteAudioSettings;
    ConnectionSettings remoteConnectionSettings;
    TransmissionSettings remoteTransmissionSettings;
    VoiceSessionSettings voiceSessionSettings;
    VoiceSession voiceSession;

    public CallManager(Client c, Socket s,int id, boolean isCaller){
        callActive=true;
        client=c;
        socket=s;
        this.id=id;
        address=socket.getInetAddress().getHostAddress();
        this.isCaller=isCaller;


    }

    public void run(){
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException ex) {
            out(" Impossbile ottenere gli stream");
        }
        sendInvite();
        while(callActive){
            out("running");
            String message=null;
            try {
                message = in.readLine();
            } catch (Exception ex) {
                out("La connessione è stata interrotta improvvisamente");
                break;
            }
            processMessage(message);
        }
        exit();
    }// end run

    private synchronized void exitNotValid(String error){
        callActive=false;
        out(error);
        return;
    }
    private synchronized void processMessage(String msg){
        if (msg==null) {
            exitNotValid("messaggio null. Esco");
        }
        if(msg.equals("BYE")){
            if(!sentBye)
                //non sono stato il primo a mandare il BYE
                sendBye();
            callActive=false;
        }
        String[] s=msg.trim().split(":",0);
        out(msg);
        //if(s.length!=2) exitNotValid("messaggio non valido: "+msg+"\nEsco");
        if(s[0].equals("CALLER")){
            setRemoteUser(s[1]);
            if(!isCaller){
                if(id!=1){
                    sendOccupied();
                }else{
                    boolean accepted=AskForAccept();
                    if(accepted)
                        sendAccept();
                    else
                        sendReject();
                }
            }

        }
        //
        else if(remoteUser!=null){
            if(s[0].equals("ACCEPT") && isCaller){
                sendParameters();
            }
            else if((s[0].equals("OCCUPIED") ||s[0].equals("REJECT"))
                && isCaller){
                sendBye();
            }
            else if(s[0].equals("PARAMETERS")){
                boolean listening=receiveParameters(s[1]);
                if(listening){
                    if (!parametersSent)
                        sendParameters();
                    sendAck();
                }
                else
                    sendBye();
            }
            else if(s[0].equals("ACK"))
                    if (voiceSession!=null){
                try {
                    voiceSession.starTransmitting();
                } catch (Exception ex) {
                    Logger.getLogger(CallManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                    }
        }
        else
            exitNotValid("ricezione dei dati prematura");
    }

    private synchronized void exit(){
        callActive=false;
        client.setFree();
        if(voiceSession!=null){
            if(voiceSession.isActive())
                voiceSession.stop();
            voiceSession=null;
        }
        close();
        out("Call Manager quit...");
    }

    private synchronized void sendOccupied(){
        sendMessage("OCCUPIED");
    }
    private synchronized void sendBye(){
        sentBye=true;
        sendMessage("BYE");
    }

    private void sendMessage(String s){
        if(out!=null && !socket.isClosed()){
            out.println(s);
            out("Sending: "+s);
        }
    }
    /**
     *  Close Stream and socket
     */
    private void close(){
        try{
            if(socket.isConnected()){
                out.close();
                in.close();
                socket.close();
            }
            out = null;
            in =null;
            socket = null;
        }catch(IOException e){
            System.err.println("Error closing connection");
        }
    }

    private void setRemoteUser(String name){
        if(remoteUser==null)
            remoteUser=name;
        else
            exitNotValid("caller already set");
    }
    private boolean receiveParameters(String p){
        String[] s=p.split(";",0);
        if(s.length<1) exitNotValid("nessun parametro :"+p);
        for (int i=0;i<s.length;i++){
            String[] t=s[i].split(",",0);
            if(t.length!=2) exitNotValid("parametro non valido :"+p);
            String property=t[0];
            String value=t[1];
            if(property.equals("RTP_PORT")){
                RTPPort=Integer.parseInt(value);
            }
            else if(property.equals("RTCP_PORT")){
                RTCPPort=Integer.parseInt(value);
            }
            else if(property.equals("RECOVERY_PORT")){
                RecoveryPort=Integer.parseInt(value);
            }
            else if(property.equals("ENCODING")){
                RemoteEncoding=Integer.parseInt(value);
            }
            else if(property.equals("WITH_BACKGROUND")){
                    withRemoteBackground=value.equals("true");
            }

        }
        remoteAudioSettings=new AudioSettings();
        remoteAudioSettings.setFormat(RemoteEncoding);
        remoteConnectionSettings=new ConnectionSettings();
        remoteConnectionSettings.setRTP(RTPPort);
        remoteConnectionSettings.setRTCP(RTCPPort);
        remoteConnectionSettings.setRecovery(RecoveryPort);
        boolean rec=client.transmissionSettings.getRecovery();
        client.transmissionSettings.setRecovery(rec && withRemoteBackground);

        voiceSessionSettings=new VoiceSessionSettings(client.audioSettings,
                                              client.connectionSettings,
                                              client.transmissionSettings,
                                              remoteAudioSettings,
                                              remoteConnectionSettings,
                                              address
                                              );
        try {
            voiceSession = new VoiceSession(voiceSessionSettings);
        } catch (SocketException ex) {
            Logger.getLogger(CallManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (voiceSession!=null){
            try {
                voiceSession.startListening();
            } catch (UnsupportedAudioFileException ex) {
                Logger.getLogger(CallManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(CallManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            return true;
        }
        //return true;
        return false;
    }
    
    private synchronized void sendParameters(){
        parametersSent=true;
        String msg="PARAMETERS:";
        String value=""+client.connectionSettings.getRTP();
        msg+="RTP_PORT,"+value+";";
        value=""+client.connectionSettings.getRTCP();
        msg+="RTCP_PORT,"+value+";";
        value=""+client.connectionSettings.getRecovery();
        msg+="RECOVERY_PORT,"+value+";";
        value=""+client.audioSettings.getFormat();
        msg+="ENCODING,"+value+";";
        value=""+client.transmissionSettings.getRecovery();
        msg+="WITH_BACKGROUND,"+value+";";
        sendMessage(msg);
       
    }

    private void sendAck(){
        sentAck=true;
        sendMessage("ACK");
    }
    private boolean AskForAccept(){
        return true;
    }

    private void sendAccept(){
        sendMessage("ACCEPT");
    }
    private void sendReject(){
        sendMessage("REJECT");
    }

    private void sendInvite(){
        sendMessage("CALLER:"+client.username);
    }
}
