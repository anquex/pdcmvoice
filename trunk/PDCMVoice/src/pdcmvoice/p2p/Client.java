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
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import static pdcmvoice.p2p.Constants.*;

/**
 *
 * @author marco
 */
public class Client extends Thread{

    String username;
    int masterPort;
    Socket socket;
    PrintWriter out;
    BufferedReader in;
    ArrayList<UserNode> online;
    private boolean doTerminate;
    private final boolean DEBUG=true;

    private JList visualList;
    private JFrame frame;


    public Client(String username, int masterport){
        this.username=username;
        this.masterPort=masterport;
        online= new ArrayList<UserNode>();

        DefaultListModel listModel = new DefaultListModel();

        visualList= new JList(online.toArray());
        visualList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        visualList.setLayoutOrientation(JList.VERTICAL);
    }

    public void connect(String server){
        connect(server, P2PPORT);
    }
    public void connect(String server, int port){
        if (server==null) server="127.0.0.1";
        try {
            socket = new Socket(server, port);
            //socket.setTcpNoDelay(true);
            //socket.setKeepAlive(true);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        start();
    }

    public void run(){

        if (socket!=null && socket.isConnected()){
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                //welcome
                out(in.readLine());
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
            out.println("USERJOIN:"+username+","+masterPort);
            while(!doTerminate){
                    String message=null;
                try {
                    message = in.readLine();
                } catch (IOException ex) {
                    if(DEBUG)
                        out("Client "+username+": disconnessione improvvisa");
                    close();
                    break;
                }
                    if (message!=null){
                        if(message.equals("BYE")){
                            out.println("BYE");
                            close();
                            break;
                        }
                        else{
                            String[] ss=message.split(":",0);
                            String action=ss[0];
                            String description=ss[1];
                            if(action.equals("USERJOIN")){
                                UserNode u =makeUserNode(description);
                                if(u!=null){
                                    add(u);
                                }else{
                                    out("Bad Parsing o bad USERJOIN");
                                }

                            }else if(action.equals("USERLEAVE")){
                                UserNode u =makeUserNode(description);
                                if(u!=null){
                                    remove(u);
                                }else{
                                    out("Bad Parsing o bad USERLEAVE");
                                }

                            }else{
                                out("Bad Command");
                            }
                        }


                    }

                    }
                    close();
                }

    }

    private void close(){
        try {
            online.clear();
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void exit(){
        doTerminate=true;
        if(socket.isConnected()){
            out.println("USERLEAVE:"+username+","+masterPort);
            close();
        }
    }

    public void add(UserNode u){
        if(DEBUG)
            out("Client "+username+": aggiunto "+u.toString());
        online.add(u);


    }

    public void remove(UserNode u){
        if(DEBUG)
            out("Client "+username+": rimosso "+u.toString());
        online.remove(u);

    }
    private UserNode makeUserNode(String description){

        String[] sss= description.split(",",0);
        String username=sss[0];
        String address=sss[1];
        String port=sss[2];
        return new UserNode(username,address,port);
    }

}
