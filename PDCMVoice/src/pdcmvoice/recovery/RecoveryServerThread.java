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
    public boolean stop; //impostato dal thread client locale 
    
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
		    br = new BufferedReader(new InputStreamReader(RecConn.getServerSocket().getInputStream()));
		    dos = new DataOutputStream(RecConn.getServerSocket().getOutputStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        while (!stop)
        {    
            boolean lineRead = false;
            while (!lineRead)
            {
                try {
                    this.lastQuery = br.readLine();
                    lineRead = true;
                } catch (IOException e) {
                    
                    if (RecoveryConnection.debug)
                       e.printStackTrace();
                    
                    
                }
            }
            
            if (lastQuery == "END OF QUERY")
            {
                stop = true;
                break;
            }
            
            StringTokenizer izer = new StringTokenizer(lastQuery, ";", false);
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
                    if (totalePkt*pktSize + pktSize >= send.length -1)
                        arrayResize(send, 2*send.length);
                    temp = RecConn.getLocalCollection().read(i);
                    System.arraycopy (temp, 0, send, totalePkt*pktSize, temp.length);
                    totalePkt++;
                   
                }
                
            }
            
            try {
                dos.write(send, 0, totalePkt*pktSize);
                dos.flush();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        }//end while (!stop)
		
        //chiusura degli stream tra server locale e client remoto
        try {
            RecConn.getServerSocket().getInputStream().close();
            RecConn.getServerSocket().getOutputStream().close();
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