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
import static pdcmvoice.impl.Constants.*;

class ClientSideClient {

    private DatagramSocket socket = null;
    private Client parent;
    String remoteAddress;//address del receiver
    
    public ClientSideClient(String remoteAddress,Client parent) throws SocketException {
        this.remoteAddress = remoteAddress;
        this.parent = parent;
        socket =  new DatagramSocket(DEFAULT_MASTER_PORT);
    }
    public boolean initConnection() throws IOException{
        String string;
        Socket s = new Socket(remoteAddress,DEFAULT_MASTER_PORT);
        DataOutputStream toServer = new DataOutputStream(s.getOutputStream());
        BufferedReader fromServer = new BufferedReader(new InputStreamReader(s.getInputStream()));
        
        toServer.writeBytes("call init");
        string = fromServer.readLine();
        s.close();
        if(string.equalsIgnoreCase("call accept"))
            return true;
        else 
            return false;
    }
    
    public boolean closeConnection() throws UnknownHostException, IOException{
        String string;
        Socket s = new Socket(remoteAddress,DEFAULT_MASTER_PORT);
        DataOutputStream toServer = new DataOutputStream(s.getOutputStream());
        BufferedReader fromServer = new BufferedReader(new InputStreamReader(s.getInputStream()));
        
        toServer.writeBytes("end call");
        string = fromServer.readLine();
        s.close();
        if(string.equalsIgnoreCase("call closed"))
            return true;
        else 
            return false;
    }

    public void endCall(){
        //da implementare
    }
    

}
