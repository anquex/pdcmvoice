/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import pdcmvoice.impl.VoiceSession;
import pdcmvoice.settings.AudioSettings;
import pdcmvoice.settings.ConnectionSettings;
import pdcmvoice.settings.TransmissionSettings;

import static pdcmvoice.impl.Constants.*;
/**
 *
 * @author marco
 */
public class Client extends Thread{

    //INFO
    String username;
    AudioSettings audioSettings;
    ConnectionSettings connectionSettings;
    TransmissionSettings transmissionSettings;
    VoiceSession vs;
    CallManager runningCallManager;

    //STATUS
    private boolean isListening;

    // WORK
    private ServerSocket serverSocket;
    private int launchedManagers;


    public Client(String name){
        this(name,
             new AudioSettings(),
             new ConnectionSettings(),
             new TransmissionSettings());
    }
    public Client(String name,AudioSettings audioSettings,
            ConnectionSettings connectionSettings,
            TransmissionSettings transmissionSettings){

        //Aggiorna i parametri in ingresso
        if (name==null || name.equals(""))
            username="UNKNOWN";
        else
            username=name;
        this.audioSettings=audioSettings;
        this.connectionSettings=connectionSettings;
        this.transmissionSettings=transmissionSettings;

        //Inizialmente Libero
        launchedManagers = 0;
        isListening      = true;
    }

    public void run(){
        out("Ricezione chiamate abilitata");
        try {
            serverSocket = new ServerSocket(connectionSettings.getMaster());
        } catch (IOException ex) {
            out("Impossibile creare il socket MASTER");
        }

        while(isListening) {
            Socket socket=null;
            try {
                socket = serverSocket.accept();
                socket.setTcpNoDelay(true);
                socket.setKeepAlive(true);
            } catch (IOException ex) {
                out("Impossibile ricevere la connessione");
            }
            if(socket!=null)
                launchCallReceiver(socket);
        }
    }// end run

    /**
     *  se non ci sono manager attivi allora è libero
     * @return
     */
    public synchronized boolean  isFree(){
        return launchedManagers==0;
    }

    /**
     *  Invocato dai managers quando escono
     * @return
     */

    public synchronized boolean  setFree(){
        launchedManagers--;
        if(launchedManagers==0){
            vs=null;
            return true;
        }
        else
            return false;
    }

    private synchronized void launchCallReceiver(Socket socket){;
            launchedManagers++;
            new CallManager(this, socket,launchedManagers,false).start();
    }

    public synchronized void call(String address,int port) throws UnknownHostException, IOException {
        if(launchedManagers>0) {
            out ("Other calls are running...");
            return; //ci sono già altre chiamate...
        }
        out("Calling "+address+":"+port);
        Socket socket=null;
            socket = new Socket();
            InetSocketAddress host=new InetSocketAddress(address, port);
            socket.connect(host, 10000);
            socket.setKeepAlive(true);
            socket.setTcpNoDelay(true);
        if(socket!=null){
            launchedManagers++;
            new CallManager(this, socket,launchedManagers,true).start();
        }
    }

    public VoiceSession getVoiceSession(){
        return vs;
    }

    public void setUsername(String username){
        this.username=username;
    }

    public void hangup(){
        if(runningCallManager!=null)
            runningCallManager.hangup();
    }

    public boolean isCalling(){
        if(runningCallManager!=null)
            return true;
        else
            return false;
    }
}