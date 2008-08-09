/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.server;

/**
 *
 * @author Laura
 */

import pdcmvoice.impl.*;
import pdcmvoice.server.Server;
import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import pdcmvoice.settings.*;
import jlibrtp.*;

public class ClientThread extends Thread {

    private Socket connectionSocket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private AudioSettings audioSettings;
    private ConnectionSettings connectionSettings;
    private TransmissionSettings transmissionSettings;
    private Information clientInformation;
    private RTPSession rtpsession;
    Information senderInfo = null;
    private String host;
    private Server server;
    private boolean listen;
    
    //client thread lato server 
     /** crea un nuovo thread per il client
      * 
      * @param socket connessione cl client
      * @param server Server con cui si connette il client
      */
    public ClientThread(Server server, Socket socket){
        super();
        connectionSocket = socket;
        this.server = server;
        listen = false;
    
    }

    
    public void run(){
        try{
            dataOutputStream = new DataOutputStream(connectionSocket.getOutputStream());
            dataInputStream = new DataInputStream(connectionSocket.getInputStream());
            listen = true;
            
        }catch(IOException e){
            System.err.println("I/O error ");
            
        }
    }     
        /** Disconnette il client dal server e chiude tutti gli stream
         *  ancorea aperti
         */
        public void disconnect(){
            listen = false;
            try{
                dataOutputStream.close();
                dataInputStream.close();
                connectionSocket.close();
            }catch(IOException e ){
                System.err.println("Error closing connection ");
            }
        }
    
        public void setClientInfo(AudioSettings as, ConnectionSettings cs,TransmissionSettings ts){
            clientInformation = new Information(as, cs, ts);
        }
    

}
