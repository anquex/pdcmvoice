package pdcmvoice.recovery;

import java.net.Socket;
import jlibrtp.RTPSession;

import java.io.*;
import java.util.StringTokenizer;

/**
 * 
 * @author Antonio
 */
public class RecoveryServerThread extends Thread
{
    private RecoveryConnection RecConn;
    private String lastQuery;
    private boolean lastQueryDone;
    public boolean stop; 
    
    public RecoveryServerThread(RecoveryConnection RecConn)
	{
		this.RecConn = RecConn;
		lastQuery = null;
		lastQueryDone = false;
		stop = false; 
	}
	
	public void run()
	{
	    BufferedReader br = null;
	    DataOutputStream dos = null;
	    
	    try {
            //DataInputStream dis = new DataInputStream(RecConn.getServerSocket().getInputStream());
		    br = new BufferedReader(new InputStreamReader(RecConn.getClientSocket().getInputStream()));
		    dos = new DataOutputStream(RecConn.getClientSocket().getOutputStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        boolean endServerThread = false; 
        
        while (!endServerThread)
        {    
            int l = 0;
            boolean lineRead = false;
            while (!lineRead)
            {
                try {
                    Thread.sleep(500); //attesa durante la ricezione
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                
                try {
                    if (RecConn.debug)
                        System.out.println("attesa query..." + l++);
                    if (br.ready())
                    {
//                        if (RecConn.debug)
//                            System.out.println("br.ready...");
//                        
                        this.lastQuery = br.readLine();
//                        if (lastQuery != null)
//                        {
                            lineRead = true;
                            System.out.println("lastQuery: " + this.lastQuery);
//                        }
                    }
                } catch (IOException e) {
                    
                    if (RecConn.debug)
                        e.printStackTrace();
                    System.out.println("ERROR:lastQuery: " + this.lastQuery);
                }
                
            
                
            }
            if (this.lastQuery != "")
            {
                if (RecConn.getLocalCollection().debug)
                    System.out.println("RICEZIONE QUERY: " + this.lastQuery);
            }
            
            if (this.lastQuery.equals("END OF QUERY"))
                endServerThread = true;
            else 
            {
                StringTokenizer izer = new StringTokenizer(this.lastQuery, ";", false);
                StringTokenizer izer2;
                int start;
                int end;
                int totalePkt = 0;
                
                int pktSize = RecConn.getLocalCollection().getPktSize();
                byte[] temp;//contiene il pacchetto associato ad ogni SN
                byte[] send = new byte[20*pktSize];//contiene 20 pacchetti (da ritrasmettere)
                
                while (izer.hasMoreTokens())
                {
                    start = -1; end = -1;
                    
                    String token = izer.nextToken();
                    izer2 = new StringTokenizer(token, "-", false);
                    
                    if (izer2.hasMoreTokens())
                        start = Integer.parseInt(izer2.nextToken());
                    if (izer2.hasMoreTokens())
                        end = Integer.parseInt(izer2.nextToken());
                    
                    if (end == -1)
                        end = start;
                    
                    for (int i = start; i <= end; i++)
                    {
                        if ((totalePkt+1)*pktSize + pktSize >= send.length -1)
                            arrayResize(send, 2*send.length);
                        temp = RecConn.getLocalCollection().read(i);
                        System.arraycopy (temp, 0, send, totalePkt*pktSize, temp.length);
                        totalePkt++;
                       
                    }
                    
                }
                
                try {
                    dos.write(send, 0, totalePkt*pktSize);
                    dos.flush();
                    lastQuery = null;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
        }//end while (!stop)
		
        //chiusura degli stream tra server locale e client remoto
        try {
            if (RecConn.getLocalCollection().debug)
                System.out.println("ServerThread: chiusura stream sul socket del client");
            RecConn.getClientSocket().getInputStream().close();//interrompe la connessione (anche l'outputStream viene chiuso)
            //RecConn.getClientSocket().getOutputStream().close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		
	}
	
	private static byte[] arrayResize(byte[] b, int newSize)
	{
	    byte[] newArray = new byte[newSize];
        System.arraycopy (b, 0, newArray, 0, b.length);
        return newArray;
	}
}