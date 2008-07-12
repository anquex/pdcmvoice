package pdcmvoice.recovery;

/*
dim pacchetto
primo SN ricevuto (SN del RTP è lo stesso del pacchetto speex correntericevuto nel RTP)
timestamp iniziale

Se non è la collezione locale: ultimo SN fino a cui ho fatto o sto facendo  richieste TCP (controllo "buchi")

array di oggetti RecoverySample: ogni oggetto RecoverySample è costituito da un SN (16bit) e da un array di byte (il pacchetto speex) lungo "dim pacchetto"

metodo add per Marco: con parametri(SN-da 16 bit-, array di byte, long timestamp)
	se il SN da inserire è precedente a "ultimo SN fino a cui ho fatto..." allora non inserisco

nelle richieste TCP devo richiedere il SN di partenza + offset dato dall'indice dell'array
	ogni richiesta rimane in sosteso finchè non sono arrivati i pckt
*/

/**
 * 
 * @author Antonio
 */
import java.io.IOException;
//import java.io.DataInputStream;

import jlibrtp.RTPSession;

public class RecoveryCollection
{
    public String type; //local oppure remote
    private int pktSize; //byte; dimesione del pacchetto codificato (speex)
    private int firstSnReceived; //uguale a quello del pkt RTP che lo contiene; costituisce l'offset per ottenere il SN esatto per il lastSampleWrote (considera l'eventuale perdita dei primi pacchetti)
    private int lastSnReceived;  //ultimoSN della collezione impostato ESPLICITAMENTE da Marco
    private long startTimestamp;
    private int encodedFormat; //vedi pdcmvoice.impl.Constants
        
        
    private RecoverySample[] collection;
    
    private int lastSn; //SN dell'ultimo sample preso in considerazione per la ricerca dei sample mancanti
    private int windowWidth; //larghezza (in pkt) della finestra per la ricerca dei sample mancanti
    
    
    
	public RecoveryCollection(String type, int pktSize, int encodedFormat)
	{
        this.type = type;
        this.pktSize = pktSize;
        firstSnReceived = -1;
        startTimestamp = -1;
        lastSn = firstSnReceived;
        lastSnReceived = -1;
        windowWidth = 50; //ricezione di 50 pkt/s --> finestra di 1s
        this.encodedFormat = encodedFormat;
        
        collection = new RecoverySample[6000]; //spazio per 2 minuti di audio
        
	}
	
	
	/**
	 * 
	 * @param sn SN del pacchetto codificato
	 * @param pkt contenuto del pacchetto codificato (speex)
	 * @param timestamp del pkt RTP
	 */
	public void add(int sn, byte[] pkt, long timestamp)
	{
	    if (firstSnReceived < 0)
	    {
    	    firstSnReceived = sn;
            startTimestamp = timestamp;
            
	    }
	    
	    int i = sn - firstSnReceived;
	    if (i  > lastSn) //aggiungi solo fuori dalla finestra di ricerca dei pkt mancanti
	    {
	        if (i >= collection.length - 20)
	            collection = collectionResize(collection, 2 * collection.length);
	        
	        collection[i] = new RecoverySample(sn, pkt);
	    }
	       
	}
	
    public void setLastSnReceived(int sn)
    {
        lastSnReceived = sn;
    }
	
    public boolean lastSnReceivedIsSetted()
    {
        return lastSnReceived > 0;
    }
    
    public int getLastSnReceived()
    {
        return lastSnReceived;
    }
    
    public int getFirstSnReceived()
    {
        return firstSnReceived;
    }
    
	public String findHoles(int window, boolean untilEnd)
	{
	    String output = "";
	    
	    
	    int start = lastSn > 0 ? lastSn+1 : 0;
	    int end;
	    if (lastSnReceived > 0 || lastSnReceived > 0 && untilEnd)
	        end = lastSnReceived;
	    else
	        end = window > 0 ?  (start + window) : (start + windowWidth);
	    lastSn = end;
	    
	    int i, j;
	    for (i = start; i <= end; i++ )
	    {
	        boolean jNull = true;
	        if (collection[i] == null)
	        {
	            if (i == end)
	                output += (firstSnReceived + i) + ";";
	            else
    	        {
    	            j = i + 1;
    	            for (; j <= end; j++ )
    	            {
    	                if (collection[j] != null)
    	                {
    	                    jNull = false;
    	                    break;
    	                }
    	            }
    	            
    	            if (!jNull)
    	            {
    	                if (i == j-1)
    	                {
    	                    output += (firstSnReceived + i) + ";";
    	                    
    	                }
    	                else
    	                {
    	                    output += (firstSnReceived + i) + "-" + (firstSnReceived + j-1) + ";";
    	                }
    	                i = j; //deve partire da j+1, ma se ne occupa il i++ del for
    	            }
    	            else
    	            {
    	                output += (firstSnReceived + i) + "-" + (firstSnReceived + j) + ";";
    	                break; //j è arrivato in fondo ed è stato inserito nella richiesta
    	            }
    	        }//end else: i < end
	        }// end if (collection[i] == null)
	    }//end for i
	    
	    return output;
	    
	   
	}
	
	public String findAllHoles()
	{
	    if (lastSnReceived == -1)
	        throw new IllegalStateException();
	    return findHoles(0, true);
	}
	
	public byte[] read(int sn)
	{
	    if (collection [sn - firstSnReceived] == null)
	        return null;
	    return collection [sn - firstSnReceived].audioPkt;
	}
	
	public void recover(int sn, byte[] pkt)
    {
	    collection[sn - firstSnReceived] = new RecoverySample(sn, pkt);
    }
	
    /*
	public boolean recover(String query, DataInputStream dis)
    {
        
        byte[] temp = new byte[pktSize];
        
        StringTokenizer izer = new StringTokenizer(query, ";", false);
        StringTokenizer izer2;
        int start;
        int end;
        
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
                try 
                {
                    dis.read(temp, 0, pktSize);
                } 
                catch (IOException e) 
                {
                    if (RecoveryConnection.debug)
                    {   e.printStackTrace();
                        return false;
                    }
                    
                }
               
                collection[i - firstSnReceived] = new RecoverySample(i, temp);
            }
            
        }
        
        return true;
        
    }
	
	*/
	
	public int getPktSize()
    {
        return this.pktSize;
    }
	
	public void setWindow(int window)
	{
	    windowWidth = window;
	}
	
	public int getEncodedFormat()
	{
	    return encodedFormat;
	}
	
	private static RecoverySample[] collectionResize(RecoverySample[] coll, int newSize)
	{
	    RecoverySample[] newCollection = new RecoverySample[newSize];
	    System.arraycopy (coll, 0, newCollection, 0, coll.length);
	    return newCollection;

	}
	
	
}
