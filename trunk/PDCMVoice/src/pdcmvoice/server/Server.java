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
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import static pdcmvoice.impl.Constants.*;

public class Server {
    
 private ServerSocket server = null;   
 private boolean listen = true;

 public Server(){     
 }
 
 /**
  * Apre il ServerSocket e rimane in ascolto per nuove connessioni
  */
 public void start(){
     try{
         server = new ServerSocket(DEFAULT_MASTER_PORT);
     }catch(IOException e){
         System.exit(-1);
     }
     while(listen){
            try {
                newClient(server.accept());
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
     }
 }
 
 /**
  * Crea un nuovo ClientThread
  * @param socket 
  */
 public void newClient(Socket socket){
     ClientThread client = new ClientThread(this, socket);
     client.start();
 }
}
