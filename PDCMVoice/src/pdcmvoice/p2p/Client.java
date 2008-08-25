/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.p2p;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
public class Client extends Thread implements ActionListener{

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
    private ProvaJpanelList userlist;
    private UserNodeListModel listmodel;


    public Client(String username, int masterport){
        this.username=username;
        this.masterPort=masterport;
        online= new ArrayList<UserNode>();
        
        //DefaultListModel listModel = new DefaultListModel();
        listmodel = new UserNodeListModel();
        userlist = new ProvaJpanelList(this, listmodel);//serve la JLIST del MAINUI
        userlist.setVisible(true);
    
        //visualList= new JList(online.toArray());
//        visualList = new JList(listmodel);
//        visualList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        visualList.setLayoutOrientation(JList.VERTICAL);
    }

    public void connect(String server){
        if(server == null) server= "127.0.0.1";
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
        listmodel.addUser(u);
        updateList();
        

    }

    public void remove(UserNode u){
        if(DEBUG)
            out("Client "+username+": rimosso "+u.toString());
        online.remove(u);
        listmodel.removeUserNode(u);
        updateList();

    }
    private UserNode makeUserNode(String description){

        String[] sss= description.split(",",0);
        String username=sss[0];
        String address=sss[1];
        String port=sss[2];
        return new UserNode(username,address,port);
    }

    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
//public static void main(String[] args) throws IOException{
//    Client c = new Client("laura",P2PPORT);
//    c.connect("127.0.0.1",P2PPORT);
//    System.in.read();
//}

    private void updateList() {
        userlist.update();
    }
}
