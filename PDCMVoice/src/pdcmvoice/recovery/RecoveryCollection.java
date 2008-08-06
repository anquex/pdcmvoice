package pdcmvoice.recovery;

/*
dim pacchetto
primo SN ricevuto (SN del RTP è lo stesso del pacchetto speex correntericevuto nel RTP)
timestamp iniziale

Se non è la collezione locale: ultimo SN fino a cui ho fatto o sto facendo  richieste TCP (controllo "buchi")

array di oggetti RecoverySample: ogni oggetto RecoverySample � costituito da un SN (16bit) e da un array di byte (il pacchetto speex) lungo "dim pacchetto"

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
    private String type; //local oppure remote
    private int pktSize; //byte; dimesione del pacchetto codificato (speex)
    private int firstSnReceived; //uguale a quello del pkt RTP che lo contiene; costituisce l'offset per ottenere il SN esatto per il lastSampleWrote (considera l'eventuale perdita dei primi pacchetti)
    public int lastSnReceived;  //ultimoSN della collezione (impostato ESPLICITAMENTE da Marco??)NO
    private long startTimestamp;
    private int encodedFormat; //vedi pdcmvoice.impl.Constants
    public boolean debug;

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
        this.debug = false;

        collection = new RecoverySample[6000]; //spazio per 2 minuti di audio

	}

	public RecoveryCollection(String type, int pktSize, int encodedFormat, boolean debug)
    {
        this.type = type;
        this.pktSize = pktSize;
        firstSnReceived = -1;
        startTimestamp = -1;
        lastSn = firstSnReceived;
        lastSnReceived = -1;
        windowWidth = 50; //ricezione di 50 pkt/s --> finestra di 1s
        this.encodedFormat = encodedFormat;
        this.debug = debug;

        collection = new RecoverySample[30000]; //spazio per 10 minuti di audio

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

	    if (sn > lastSnReceived)
	        lastSnReceived = sn;

	    int i = sn - firstSnReceived;
	    if (i  > lastSn) //aggiungi solo fuori dalla finestra di ricerca dei pkt mancanti
	    {
	        if (i >= collection.length - 20)
	            collection = collectionResize(collection, 2 * collection.length);

	        collection[i] = new RecoverySample(sn, pkt);

	        System.out.println(type + ": inserito sn " + sn);
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

	    if (firstSnReceived >= 0)
	    {
    	    int start = lastSn > 0 ? lastSn+1 : 0;
    	    int end;
    	    /*
    	    if (lastSnReceived > 0 || lastSnReceived > 0 && untilEnd)
    	    */
    	    if(untilEnd)
    	        end = lastSnReceived;
    	    else
    	        end = window > 0 ?  (start-1 + window) : (start-1 + windowWidth);
    	    lastSn = end;

    	    if (debug)
    	        System.out.println("findHoles - start: " + start + ", end: " + end);

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
        	                    jNull = false; //j punta ad un valore non nullo
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
        	                break; //j e' arrivato in fondo ed e' stato inserito nella richiesta
        	            }
        	        } //end else: i < end
    	        } // end if (collection[i] == null)
    	    } //end for i

	    }//end if

	    return output;


	}

	public byte[] findHolesByte(int window, boolean untilEnd)
    {
        byte[] output = new byte[10*Math.max(window, windowWidth)];
        int k = 3;
        byte next = 0; //separatore
        byte until = 1; //separatore per intervalli
        byte endOfQuery = 2;

        //short e1 = 0;
        //short e2 = 0;
        Integer t = null;

        //byte mask = Byte.parseByte("1111111100000000", 2);
        int hiMask = Integer.parseInt("65280", 10);//1111111100000000
        int lowMask = Integer.parseInt("255", 10);//0000000011111111

        if (firstSnReceived >= 0)
        {
            int start = lastSn > 0 ? lastSn+1 : 0;
            int end;
            /*
            if (lastSnReceived > 0 || lastSnReceived > 0 && untilEnd)
            */
            if(untilEnd)
                end = lastSnReceived;
            else
                end = window > 0 ?  (start-1 + window) : (start-1 + windowWidth);
            lastSn = end;

            if (debug)
                System.out.println("findHolesByte - start: " + start + ", end: " + end);

            int i, j;
            int firstSnOfTheQuery = 0;

            System.out.println("firstSnReceived: " + firstSnReceived);
            for (i = start; i <= end; i++ )
            {

                boolean jNull = true;
                if (collection[i] == null)
                {
                    System.out.println("firstSnOfTheQuery: " + firstSnOfTheQuery);
                    if (i == end)
                    {
                        k = writeOutSn(output, k, (firstSnReceived + i - firstSnOfTheQuery), next);
                        System.out.println("query-write: " + (firstSnReceived + i - firstSnOfTheQuery));
                    }
                    else
                    {
                        j = i + 1;
                        for (; j <= end; j++ )
                        {
                            if (collection[j] != null)
                            {
                                jNull = false; //j punta ad un valore non nullo
                                break;
                            }
                        }

                        if (!jNull)
                        {
                            if (i == j-1)
                            {
                                k = writeOutSn(output, k, (firstSnReceived + i - firstSnOfTheQuery), next);
                                System.out.println("query-write: " + (firstSnReceived + i - firstSnOfTheQuery));

                                if (firstSnOfTheQuery == 0)
                                    firstSnOfTheQuery = firstSnReceived + i;
                            }
                            else
                            {
                                k = writeOutSn(output, k, (firstSnReceived + i - firstSnOfTheQuery), until);
                                System.out.println("query-write: " + (firstSnReceived + i - firstSnOfTheQuery));

                                if (firstSnOfTheQuery == 0)
                                    firstSnOfTheQuery = firstSnReceived + i;

                                k = writeOutSn(output, k, (firstSnReceived + j-1 - firstSnOfTheQuery), next);
                                System.out.println("query-write: " + (firstSnReceived + j-1 - firstSnOfTheQuery));

                            }


                            i = j; //deve partire da j+1, ma se ne occupa il i++ del for
                        }
                        else
                        {
                            k = writeOutSn(output, k, (firstSnReceived + i - firstSnOfTheQuery), until);
                            System.out.println("query-write: " + (firstSnReceived + i - firstSnOfTheQuery));

                            if (firstSnOfTheQuery == 0)
                                firstSnOfTheQuery = firstSnReceived + i;

                            k = writeOutSn(output, k, (firstSnReceived + j - firstSnOfTheQuery), next);
                            System.out.println("query-write: " + (firstSnReceived + j - firstSnOfTheQuery));


                            break; //j e' arrivato in fondo ed e' stato inserito nella richiesta


                        }
                    } //end else: i < end


                } // end if (collection[i] == null)
            } //end for i

        }//end if

        //lunghezza della query vera e propria (esclusi i 3 byte che indicano la lunghezza stessa)
        System.out.println("lunghezza: " + (k-3));
        writeOutSn(output, 0, (k-3), next);
        //System.out.println("ultima k: " + k);

        //k = writeOutSn(output, k, -1000, endOfQuery);

        byte[] out = new byte[k];
        System.arraycopy(output, 0, out, 0, k);

        return out;


    }

	public String findAllHoles()
	{
	    if (lastSnReceived == -1)
	        throw new IllegalStateException();
	    return findHoles(0, true);
	}

	public byte[] findAllHolesByte()
    {
        if (lastSnReceived == -1)
            throw new IllegalStateException();
        return findHolesByte(0, true);
    }

	public byte[] read(int sn)
	{
	    if (collection [sn - firstSnReceived] == null)
	        return null;
	    return collection [sn - firstSnReceived].audioPkt;
	}

	public void recover(int sn, byte[] pkt)
    {
	    if (sn - firstSnReceived >= collection.length - 20)
            collection = collectionResize(collection, 2 * collection.length);

	    collection[sn - firstSnReceived] = new RecoverySample(sn, pkt);
    }

	private int writeOutSn(byte[] array, int start, int sn, byte nextByte) //sn  sempre di 2 byte al massimo
	{
	    if (sn > 0)
	    {
//    	    System.out.println("--writeOutSn-- sn: " + sn);

	        Integer t;
    	    int hiMask = Integer.parseInt("65280", 10);//1111111100000000
            int lowMask = Integer.parseInt("255", 10); //0000000011111111

            t = new Integer((hiMask & sn)>>8);
    	    array[start++] = t.byteValue();
//    	    System.out.println("--writeOutSn-- bit pi significativi: " + t.byteValue());
            t = new Integer(lowMask & sn);
            array[start++] = t.byteValue();
//          System.out.println("--writeOutSn-- bit meno significativi: " + t.byteValue());
            array[start++] = nextByte;
	    }
	    else
	        array[start++] = nextByte;

        return start; //prossima posizione in cui scrivere
	}

	public static int mergeBytes(byte a, byte b)
    {
//	    System.out.println("--mergeBytes-- a: " + a + " b: " + b);

	    int aM = (int) a & 0xff;
	    int bM = (int) b & 0xff;

	    aM = aM << 8;
//	    System.out.println("--mergeBytes-- bit pi significativi: " + aM);
//      System.out.println("--mergeBytes-- bit meno significativi: " + bM);
	    return aM + bM;



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

	public int getWindowWidth()
	{
	    return windowWidth;
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
