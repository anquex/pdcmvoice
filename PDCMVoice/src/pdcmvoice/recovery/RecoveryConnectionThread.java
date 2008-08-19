package pdcmvoice.recovery;

import jlibrtp.RTPSession;
import pdcmvoice.impl.*;
import pdcmvoice.recovery.*;
import pdcmvoice.settings.*;
import java.net.Socket;
import java.net.ServerSocket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.SocketTimeoutException;

public class RecoveryConnectionThread extends Thread {
    
    VoiceSessionSettings settings;
    RTPSession rtpSession;
    VoiceSession voiceSession;
    RecoveryCollection localCollection;
    RecoveryCollection remoteCollection;
    RecoveryServerThread rs;
    RecoveryClientThread rc;
    
    
    
    public RecoveryConnectionThread(VoiceSessionSettings settings, RTPSession rtpSession, VoiceSession voiceSession, RecoveryCollection localCollection, RecoveryCollection remoteCollection)
    {
        this.settings = settings;
        this.rtpSession = rtpSession;
        this.voiceSession = voiceSession;
        this.localCollection = localCollection;
        this.remoteCollection = remoteCollection;
        rs = null;
        rc = null;
    }
    
    public void run()
    {
        System.out.println("--RECOVERY-- RecoveryConnectionThread avviato");
      //RECOVERY COLLETION FROM SETTINGS
        //settings.withRecovery()
      
        if (settings.withRecovery())
        {
            Socket client = null;
            Socket server = null;
            
            ServerSocket serverSocket = null;
  
            try {
                serverSocket = new ServerSocket(settings.getLocalRecoveryPort());
                serverSocket.setSoTimeout(1000);
                
            } catch (IOException e) {
                // TODO Auto-generated catch block
                if (pdcmvoice.impl.Constants.RECOVERY_CONNECTION_DEBUG)
                    e.printStackTrace();
            }
            
          //##CONNESSIONE PER IL SISYEMA DI RECOVERY
            System.out.println("--RECOVERY-- Connessione in corso...");
            
            while (client == null || server == null)
            {
                try {
                    Thread.sleep(500);
                    if (pdcmvoice.impl.Constants.RECOVERY_CONNECTION_DEBUG)
                       System.out.println("--RECOVERY-- Tentativo di connessione");
                } catch (InterruptedException e2) {
                    // TODO Auto-generated catch block
                    if (pdcmvoice.impl.Constants.RECOVERY_CONNECTION_DEBUG)
                        e2.printStackTrace();
                }
                
                if (client == null)
                {
                    try {
                        client = new Socket(settings.getRemoteAddress(), settings.getRemoteRecoveryPort());
                        System.out.println("--RECOVERY-- socket CLIENT ok");
                    } catch (UnknownHostException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                        client = null;
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        if (pdcmvoice.impl.Constants.RECOVERY_CONNECTION_DEBUG)
                            e1.printStackTrace();
                        client = null;
                    }
                }
                
                    
                
                if (server == null)
                {
                    try {
                        server = serverSocket.accept();
                        System.out.println("--RECOVERY-- socket SERVER ok");
                    }
                    
                    catch (SocketTimeoutException e) {
                        // TODO Auto-generated catch block
                        if (pdcmvoice.impl.Constants.RECOVERY_CONNECTION_DEBUG)
                            System.out.println("TENTATIVO DI RICONNESSIONE socket server per Timeout (RECOVERY)...");
                        if (pdcmvoice.impl.Constants.RECOVERY_CONNECTION_DEBUG)
                            e.printStackTrace();
                        server = null;
                    }
                    catch (IOException e) {
                        // TODO Auto-generated catch block
                        if (pdcmvoice.impl.Constants.RECOVERY_CONNECTION_DEBUG)
                            e.printStackTrace();
                        server = null;
                    }
                }
                
                    
            }
  
            
  
            RecoveryConnection recoveryConnection = new RecoveryConnection(server, localCollection, client, remoteCollection, rtpSession, pdcmvoice.impl.Constants.RECOVERY_CONNECTION_DEBUG);
  
            rs = new RecoveryServerThread(recoveryConnection, voiceSession);
            rc = new RecoveryClientThread(recoveryConnection, voiceSession);
  
            
  
         
            
            
  //          settings.getRemoteRecoveryPort(); 
  //          settings.getLocalRecoveryPort();
  
        }
    }
    
    public RecoveryServerThread getRs()
    {
        return rs;
    }
    
    public RecoveryClientThread getRc()
    {
        return rc;
    }

}
