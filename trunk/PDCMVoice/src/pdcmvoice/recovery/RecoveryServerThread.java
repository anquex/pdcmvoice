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
    private byte[] lastQueryByte;
    public boolean stop; 
    
    public RecoveryServerThread(RecoveryConnection RecConn)
	{
		this.RecConn = RecConn;
		lastQuery = null;
		lastQueryByte = null;
		lastQueryDone = false;
		stop = false; 
	}
	
	public void run()
	{
	    DataInputStream dis = null;
	    DataOutputStream dos = null;
	    
	    try {
            //DataInputStream dis = new DataInputStream(RecConn.getServerSocket().getInputStream());
		    dis = new DataInputStream(RecConn.getClientSocket().getInputStream());
		    dos = new DataOutputStream(RecConn.getClientSocket().getOutputStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        boolean endServerThread = false; 
        
        while (!endServerThread)
        {    
            int l = 0; //attesa input
            boolean queryRead = false;
            boolean lengthRead = false;
            int queryLength = 0;
            int f = 0; //lettura lunghezza
            lastQueryByte = null;
            int k = 0; // indice lastQueryByte
            
            while (!endServerThread && (!lengthRead || !queryRead))
            {
//                try {
//                    Thread.sleep(5); //attesa durante la ricezione
//                } catch (InterruptedException e1) {
//                    // TODO Auto-generated catch block
//                    e1.printStackTrace();
//                }
                
                if (!lengthRead)
                {
                    try {
                        if (RecConn.debug)
                            System.out.println("attesa lunghezza query..." + l++);
                        //if (dis.available() > 0)
                        {
    //                        if (RecConn.debug)
    //                            System.out.println("br.ready...");
    //                        
                            queryLength += dis.readByte();
                            if (RecConn.debug)
                                System.out.println("una parte della lunghezza e' stata letta");
                            f++;
                            if (f >= 2)
                            {
                                lengthRead = true;
                                //CONTROLLO COMUNICAZIONE "FINE DELLE RICHIESTE" DA PARTE DEL CLIENT
                                
                                if (queryLength == 0 && dis.readByte() == 2) //salto il byte di controllo
                                {
                                    endServerThread = true;
                                    if (RecConn.getLocalCollection().debug)
                                        System.out.println("--SERVER-- Client says: END OF QUERY ");
                                    //lastQueryByte rimane null
                                }
                                else
                                {
                                    dis.readByte();//salto il byte di controllo
                                    lastQueryByte = new byte[queryLength];
                                    
                                    //lunghezza della query vera e propria (esclusi i 3 byte che indicano la lunghezza stessa)
                                    if (RecConn.getLocalCollection().debug)
                                        System.out.println("--SERVER-- queryLength: " + queryLength);
                                }
                            }
                        }
                    } catch (IOException e) {
                        
                        if (RecConn.debug)
                            e.printStackTrace();
                        System.out.println("ERROR:queryLength: " + queryLength);
                    }
                }// end if (!lengthRead)
                
                else if (!queryRead)
                {
                    try {
//                        if (RecConn.debug)
//                            System.out.println("attesa query..." + l++);
                        //if (dis.available() > 0)
                        {
    //                        if (RecConn.debug)
    //                            System.out.println("br.ready...");
    //                        
                            lastQueryByte[k++]= dis.readByte();
                            
//                            if (RecConn.debug)
//                                System.out.println(k + " - esimo byte della richiesta letto ");
                            
                            if (k >= queryLength)
                            {
                                queryRead = true;
                                //lunghezza della query vera e propria (esclusi i 3 byte che indicano la lunghezza stessa)
                            }
                        }
                    } catch (IOException e) {
                        
                        if (RecConn.debug)
                            e.printStackTrace();
                    }
                }// end if (!queryRead)
            
                
            }
            
            if (this.lastQueryByte != null)
            {
                if (RecConn.getLocalCollection().debug)
                {
                    System.out.print("---RICEZIONE QUERY:");
                    
                    for (int i = 0; i<= lastQueryByte.length -1; i++)
                        System.out.print(" " + lastQueryByte[i]);
                    
                    System.out.println("");
                }
            }
            else
                if (RecConn.getLocalCollection().debug)
                {
                    System.out.print("-------ERRORE RICEZIONE QUERY-------");
                }
            
            if (!endServerThread);
            {
                //queryLength = lunghezza della query vera e propria (esclusi i 3 byte che indicano la lunghezza stessa)
                int sn  = 0;
                byte separatore = 0;
                int start, end;
                
                int totalePkt = 1;
                
                int pktSize = RecConn.getLocalCollection().getPktSize();
                byte[] temp;//contiene il pacchetto associato ad ogni SN
                byte[] send = new byte[(RecConn.getLocalCollection().getWindowWidth())*pktSize];//contiene al massimo windowWidth pacchetti (da ritrasmettere)
                
                int firstSnOfTheQuery = 0;
                
                for (int j = 0; j<=queryLength -1; j++) //diverso dal for del RecoveryClientThread
                {
                    sn = lastQueryByte[j] + lastQueryByte[++j];
                    separatore = lastQueryByte[++j];
                    
                    if (j == 2)
                        firstSnOfTheQuery = sn;
                    else
                        sn += firstSnOfTheQuery; //considero vecchio sn come incremento
                    
                    
                    start = sn;
                    if (separatore == 0)
                        end = start;
                    else if (separatore == 1)
                    {
                        end = firstSnOfTheQuery + lastQueryByte[++j] + lastQueryByte[++j];
                        j++; //salto il separatore successivo
                        
                        if (RecConn.getLocalCollection().debug)
                            System.out.println("--SERVER-- end: " + end);
                    }
                    else
                        throw new IllegalArgumentException("posizione " + j);
                    
                    for (int i = start; i <= end; i++)
                    {
                        if ((totalePkt-1)*pktSize + pktSize -1 >= send.length -1)
                            send = arrayResize(send, 2*send.length);
                        
                        temp = RecConn.getLocalCollection().read(i);
                        
                        try {
                            System.arraycopy (temp, 0, send, (totalePkt-1)*pktSize, temp.length);
                        } catch (NullPointerException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            if (RecConn.getLocalCollection().debug)
                                System.out.println("pacchetto " + i + "non ancora inserito nella localCollection");
                        }
                        
                        totalePkt++;
                        
                      if (RecConn.getLocalCollection().debug && temp != null)
                        {
                        System.out.print("--SERVER-- Inviato Sn " + i + ": ");
                        
                        for (int p = 0; p <= temp.length -1; p++)
                        {
                            System.out.print(temp[p] + " ");
                        }
                        
                        System.out.println("");
                        }
                       
                    }
                    
                }
                
                try {
                    
                    if (RecConn.getLocalCollection().debug)
                        System.out.println("--SERVER-- tentativo invio pacchetti richiesti ");
                    
                    dos.write(send, 0, (totalePkt-1)*pktSize);
                    dos.flush();
                    
                    if (RecConn.getLocalCollection().debug)
                        System.out.println("--SERVER-- pacchetti INVIATI");
                    
                    lastQueryByte = null;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
            }
            
        }//end while (!lengthRead || !queryRead)
		
        //chiusura degli stream tra server locale e client remoto
        try {
            if (RecConn.getLocalCollection().debug)
                System.out.println("_____________________________ServerThread: chiusura stream sul socket del client");
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