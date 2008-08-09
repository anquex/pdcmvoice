/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.client;

/**
 *
 * @author Laura
 */

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import jlibrtp.RTPSession;
import pdcmvoice.impl.Information;
import pdcmvoice.impl.VoiceSession;
import pdcmvoice.impl.VoiceSessionReceiver;
import static pdcmvoice.impl.Constants.*;
import pdcmvoice.settings.*;


public class Client {
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private AudioSettings audioSettings;
    private ConnectionSettings connectionSettings;
    private TransmissionSettings transmissionSettings;
    private Information clientInformation;
    private String host;
    private Socket connectionSocket;
    private ClientSideClient connectionClient;
    private ClientSideServer connectionServer;
    private RTPSession rtp;
    private VoiceSessionReceiver receiverSession;
    
    public Client(String host) throws IOException{
        this.host = host;        
    }
    
    public void connect(){
        try{
            connectionSocket = new Socket(host, connectionSettings.getMaster());
            dataInputStream = new DataInputStream(connectionSocket.getInputStream());
            dataOutputStream = new DataOutputStream(connectionSocket.getOutputStream());
        }catch(UnknownHostException e1){
            System.err.println("Unknown host:" + host);
            System.exit(-1);
        }catch(IOException e2){
            System.err.println("Error connection");
            System.exit(-1);
        }
        
    }
    //sconnette e chiude il programma 
    public void exit(){
        System.out.println("The connection will be close....");
        disconnect();
        System.out.println("...PDCMVoice will be close...");
        System.out.println("BYE");
        System.exit(-1);
    }
    //chiude gli stream e il socket
    public void disconnect(){
        try{
            if(connectionSocket.isConnected()){
                dataOutputStream.close();
                dataInputStream.close();
                connectionSocket.close();
            }
            dataOutputStream = null;
            dataInputStream =null;
            connectionSocket = null;            
        }catch(IOException e){
            System.err.println("Error closing connection");
        }        
    }

    public void call(VoiceSessionSettings vss){
        try {
            VoiceSession session = new VoiceSession(vss);
            session.start();
        } catch (SocketException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception e){
            System.err.println("Error.....");
        }
        
    }
    /**
     * 
     * @param vs 
     */
    public void answerCall(VoiceSessionSettings vs){
        String addr = vs.getRemoteAddress();
        if(addr != null){
            try{               
                connectionClient = new ClientSideClient(addr,this);
                connectionServer = new ClientSideServer();
                (new Thread(connectionServer)).start();
                StartServer.callActive = true;
                
                
            }catch(Exception e){
                
            }
        }
    }

    void refuseCall() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
