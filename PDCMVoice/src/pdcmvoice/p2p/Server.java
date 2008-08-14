/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.p2p;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static pdcmvoice.p2p.Constants.*;

/**
 *
 * @author marco
 */
public class Server extends Thread{

    private List<UserNode> users;
    private List<ServerThread> servants;
    private ServerSocket serverSocket;

    public Server(){
        users= new ArrayList<UserNode>();
        servants=new ArrayList<ServerThread>();

    }

    public void run(){
        try {
            serverSocket = new ServerSocket(P2PPORT);
        } catch (IOException ex) {
            out("Impossibile creare il socket P2P");
        }
        out("In ascolto sulla porta "+P2PPORT);
        Socket socket=null;
        while(true){
            out("1");
            try {
                socket = serverSocket.accept();
                out("connessione accettata");
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                out("impossibile stabilire la connessione con il client");
            }
//            try {
//                //socket.setKeepAlive(true);
//                //socket.setTcpNoDelay(true);
//            } catch (SocketException ex) {
//                out("impostazione keep alive fallita");
//            }
            if (socket!=null){
                ServerThread se=new ServerThread(this, socket);
                servants.add(se);
                se.start();
            }
        }
    }

    public synchronized void userExit(UserNode u){
        out("2");
        if(u==null) return;
        if(!users.remove(u)){
            out("Ho tentato di rimuovere un nodo "+u.toString()+" che non c'era...   non dovrebbe succedere.");
        }
        out("Rimuovo "+u.toString());
        Iterator<ServerThread> s= servants.iterator();
        while(s.hasNext()){
            ServerThread userServer=s.next();
            if (userServer.userNode().equals(u)){

                servants.remove(userServer);
                // eventualmente saluta

            }else{
                userServer.userExit(u);
            }
        }
    }
    public synchronized void userJoin(UserNode u){
        if(users.contains(u)){
            out("Ho tentato di aggiungere un nodo già presente");
            return;
        }
        out("Aggiungo "+u.toString());
        Iterator<ServerThread> s= servants.iterator();
        while(s.hasNext()){
            ServerThread userServer=s.next();
            if (userServer.userNode().compareTo(u)==0){
                Iterator<UserNode> userIter=users.iterator();
                while(userIter.hasNext()){
                    userServer.userJoin(userIter.next());
                }
            }
            else
                userServer.userJoin(u);
        }
        users.add(u);

    }

    /**
     *  Rimuove il ServerThread. Utilizzato per rimuovere un ServerThread
     *  che non ha completato con successo l'aggiunta di un nuovo UserNode
     * @param s
     */
    public synchronized void removeServerThread(ServerThread s){
        servants.remove(s);
    }

    public static void main(String[] args){
        new Server().start();
    }



}
