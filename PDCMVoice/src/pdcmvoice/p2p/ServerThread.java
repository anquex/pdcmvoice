/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.p2p;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static pdcmvoice.p2p.Constants.*;

/**
 *
 * @author marco
 */
public class ServerThread extends Thread{
    
    private Server server;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private UserNode associatedUser;

    public ServerThread (Server s,Socket so){
        server = s;
        socket = so;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void run(){

        out.println("Welcome to PDCMVOICE P2P");
        while(true){
            out("4");
            String message=null;
            try {
                message = in.readLine();
            } catch (SocketException ex) {
                server.removeServerThread(this);
                if(associatedUser!=null)
                    server.userExit(associatedUser);
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                break;
            }catch (IOException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (message!=null){
                if(message.equals("BYE")){
                    out.println("BYE");
                    close();
                }
                else{
                    String[] ss=message.split(":",0);
                    String action=ss[0];
                    String description=ss[1];
                    if(action.equals("USERJOIN")){
                        UserNode u =makeUserNode(description);
                        if(u!=null){
                            associatedUser=u;
                            server.userJoin(u);
                        }else{
                            out("Bad Parsing o bad USERJOIN");
                        }

                    }else if(action.equals("USERLEAVE")){
                        server.removeServerThread(this);
                        out.println("BYE");
                        server.userExit(associatedUser);

                    }else{
                        out("Bad Command");
                    }
                }


            }
        }
        
    }

    public void userJoin(UserNode u){
        out.println("USERJOIN:"+u.toString());

    }

    public void userExit(UserNode u){
        out.println("USERLEAVE:"+u.toString());

    }

    public void sendUserList(List<UserNode> online){

    }

    private void close(){

    }

    public UserNode userNode(){
        return associatedUser;
    }

    private UserNode makeUserNode(String description){

        String[] sss= description.split(",",0);
        String username=sss[0];
        String port=sss[1];
        String address=socket.getInetAddress().getHostAddress();
        return new UserNode(username,address,port);
    }
}
