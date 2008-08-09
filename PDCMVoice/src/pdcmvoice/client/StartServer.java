/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.client;

/**
 *
 * @author Laura
 */
import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import pdcmvoice.impl.Information;
import static pdcmvoice.impl.Constants.*;


class StartServer implements Runnable {

    public static boolean callActive= false;
    static InetAddress localAddress;
    private ServerSocket startSocket;
    private Socket socket;
    
    protected Client parentClient;
    
    public StartServer(Client parent){
        parentClient = parent;
    }

    public void run() {
        try{
            startSocket = new ServerSocket(DEFAULT_MASTER_PORT);
            while(true){
                try{
                    socket = startSocket.accept();
                }catch(Exception e){
                    System.err.println("Impossibile accetare la connessioen");
                    return;
                }
                ServerWorker worker = new ServerWorker(socket, this);
                (new Thread(worker)).start();
                 startSocket.close();
            }
           
        }catch (Exception e1){
            e1.printStackTrace();
        }
    }
    
}
class ServerWorker implements Runnable{
    Socket socket;
    StartServer parentServer;
    DataOutputStream dos;
    private boolean acceptCall;
    String query;
 
    private Information informationClient;
    
    public ServerWorker(Socket socket, StartServer parent){
        this.socket = socket;
        parentServer = parent;
    }
    
    public void run(){
        try {
            try {
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                informationClient = (Information) in.readObject();
                dos = new DataOutputStream(socket.getOutputStream());
                query = (new BufferedReader(new InputStreamReader(socket.getInputStream()))).readLine().trim();

                StartServer.localAddress = socket.getLocalAddress();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (query.equalsIgnoreCase("call init")) {
                Client sender = parentServer.parentClient;
                if(StartServer.callActive || sender == null){
                    dos.writeBytes("call refused");
                }else{
                    dos.writeBytes("call accept");
                    parentServer.parentClient.answerCall(informationClient.getVoiceSessionSettings());
                } 
                }else if (query.equalsIgnoreCase("end call")) {
                dos.writeBytes("call closed");
                parentServer.parentClient.refuseCall();
            }
            
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
            
    }
}
