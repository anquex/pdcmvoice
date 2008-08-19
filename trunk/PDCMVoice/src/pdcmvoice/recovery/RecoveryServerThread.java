package pdcmvoice.recovery;

import java.net.Socket;
import jlibrtp.RTPSession;
import pdcmvoice.impl.*;

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
    private VoiceSession voiceSession;
    public boolean stop; 
    
    public RecoveryServerThread(RecoveryConnection RecConn, VoiceSession voiceSession)
	{
		this.RecConn = RecConn;
		lastQuery = null;
		lastQueryByte = null;
		lastQueryDone = false;
		stop = false; 
		this.voiceSession = voiceSession;
	}
	
	public void run()
	{
	    if (voiceSession != null && RecConn.getRemoteCollection().getPktSize() <= 0)
        {
    	  //##ACQUISIZIONE DIMENSIONE IN BYTE DI UN PACCHETTO VOCE CODIFICATO
    	    while(voiceSession.lastEncodedFrameSize() <= 0)
    	    {
    	        if (RecConn.debug)
                    System.out.println("--SERVER-- Acquisizione dimensione pacchetto codificato");
    	        
    	        try {
                    Thread.sleep(500); //attesa durante l'acquisizione della dimensione
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
    	    }
    	    
    	    this.RecConn.getLocalCollection().setPktSize(voiceSession.lastEncodedFrameSize());
    	    if (RecConn.debug)
                System.out.println("--SERVER-- Dimensione pacchetto codificato: " + voiceSession.lastEncodedFrameSize());
        }
	    
	  //##APERTURA DEGLI STREAM SUL CLIENTSOCKET
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
        
        
//        try {
//            Thread.sleep(500); //attesa prima di partire
//        } catch (InterruptedException e1) {
//            // TODO Auto-generated catch block
//            e1.printStackTrace();
//        }
        
        
        boolean endServerThread = false; 
        
        while (!endServerThread)
        {    
            int l = 0; //attesa input
            boolean queryRead = false;
            boolean lengthRead = false;
            int queryLength = 0;
            byte queryLength1 = 0;
            byte queryLength2 = 0;
            int f = 0; //lettura lunghezza
            lastQueryByte = null;
            int k = 0; // indice lastQueryByte
            
          //##ANALISI DELLO STREAM IN INPUT: VERIFICA FINE RICHIESTE DA PARTE DEL CLIENT REMOTO E ANALISI LUNGHEZZA E CONTENUTO DELLA QUERY
            while (!endServerThread && (!lengthRead || !queryRead))
            {
                try {
                    Thread.sleep(20); //attesa durante la ricezione
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                
              //##LETTURA LUNGHEZZA QUERY E VERIFICA DELL'EVENTUALE COMUNICAZIONE "END OF QUERY" 
                if (!lengthRead)
                {
                    try {
                        if (dis.available() > 0)
                        {
                        if (RecConn.debug)
                            System.out.println("attesa lunghezza query..." + l++);
                        
    //                        if (RecConn.debug)
    //                            System.out.println("br.ready...");
    //                        
                            if (f == 0)
                                queryLength1 = dis.readByte();
                            else if (f == 1)
                                queryLength2 = dis.readByte();
                            if (RecConn.debug)
                                System.out.println("una parte della lunghezza e' stata letta");
                            f++;
                            if (f >= 2)
                            {
                                
                              //##CONTROLLO COMUNICAZIONE "FINE DELLE RICHIESTE" DA PARTE DEL CLIENT
                                queryLength = RecoveryCollection.mergeBytes(queryLength1, queryLength2);
                                //if (queryLength == 0 && dis.readByte() == 2) //salto il byte di controllo
                                if (queryLength == 0)
                                {
                                    endServerThread = true;
                                    if (RecConn.getLocalCollection().debug)
                                        System.out.println("--SERVER-- Client says: END OF QUERY ");
                                    //lastQueryByte rimane null
                                }
//                                else if (queryLength == 0)
//                                {
//                                    if (RecConn.getLocalCollection().debug)
//                                        System.out.println("--SERVER-- EMPTY QUERY RECEIVED");
//                                    dis.readByte();//salto il byte di controllo
//                                    f= 0; //pronto a leggere una nuova queryLength
//                                }
                                else
                                {
                                    lengthRead = true;
                                    dis.readByte();//salto il byte di controllo
                                    lastQueryByte = new byte[queryLength];
                                    
                                    //lunghezza della query vera e propria (esclusi i 3 byte che indicano la lunghezza stessa)
                                    if (RecConn.getLocalCollection().debug)
                                        System.out.println("--SERVER-- queryLength: " + queryLength);
                                }
                            }
                        }//end if (dis.available() > 0)
                        else if (RecConn.getLocalCollection().debug)
                        {
                            System.out.println("--SERVER-- DATA INPUT STREAM (lettura length) ASSENTE");
                        }
                     
                    } catch (IOException e) {
                        
                        if (RecConn.debug)
                            e.printStackTrace();
                        System.out.println("ERROR:queryLength: " + queryLength);
                    }
                }// end if (!lengthRead)
                
              //##RICEZIONE DELLA QUERY
                else if (!queryRead)
                {
                    try {
//                        if (RecConn.debug)
//                            System.out.println("attesa query..." + l++);
                        if (dis.available() > 0)
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
                        }//end if (dis.available() > 0)
                        else if (RecConn.getLocalCollection().debug)
                        {
                            System.out.println("--SERVER-- DATA INPUT STREAM (lettura query) ASSENTE");
                        }
                    } catch (IOException e) {
                        
                        if (RecConn.debug)
                            e.printStackTrace();
                    }
                }// end if (!queryRead)
            
                
            }
            
          //##DEBUG
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
                if (RecConn.getLocalCollection().debug && !endServerThread)
                {
                    System.out.println("-------ERRORE RICEZIONE QUERY-------");
                }
            
          //##ELABORAZIONE DELLA QUERY
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
                
                int firstSnOfTheQuery = 0;//significato locale alla query
                
              //##ELABORAZIONE DELLA QUERY RICEVUTA
                for (int j = 0; j<=queryLength -1; j++) //diverso dal for del RecoveryClientThread
                {
                  //##LETTURA DEL VALORE SUCCESSIVO
                    sn = RecoveryCollection.mergeBytes(lastQueryByte[j], lastQueryByte[++j]);
                    separatore = lastQueryByte[++j];
                    
                  //##DETERMINAZIONE DEL PRIMO SN DELLA QUERY A CUI TUTTI I SUCCESSIVI VALORI DI OFFSET DELLA QUERY STESSA FARANNO RIFERIMENTO
                    if (j == 2)
                        firstSnOfTheQuery = sn;
                    else
                        sn += firstSnOfTheQuery; //considero vecchio sn come incremento
                    
                  //##DETERMINAZIONE DEGLI INDICI START ED END
                    start = sn;
                    if (separatore == 0)
                        end = start;
                    else if (separatore == 1)
                    {
                        end = firstSnOfTheQuery + RecoveryCollection.mergeBytes(lastQueryByte[++j], lastQueryByte[++j]);
                        j++; //salto il separatore successivo
                        
                        if (RecConn.getLocalCollection().debug)
                            System.out.println("--SERVER-- end: " + end);
                    }
                    else
                        throw new IllegalArgumentException("posizione " + j);
                    
                  //##RECUPERO PACCHETTI TRA GLI INDICI START ED END (AL LIMITE START = END ==> PACCHETTO ISOLATO)
                    for (int i = start; i <= end; i++)
                    {
                        if ((totalePkt-1)*pktSize + pktSize -1 >= send.length -1)
                            send = arrayResize(send, 2*send.length);
                        
                        //temp = RecConn.getLocalCollection().read(i);
                        
                        //##LEGGI IL PACCHETTO DALLA COLLEZIONE
                        int n = 1;
                        while (n <= 5 && (RecConn.getLocalCollection().read(i)== null || RecConn.getLocalCollection().read(i).length == 0))
                        {
                            try {
                              Thread.sleep(100); //attesa durante la lettura dei pacchetti richiesti
                              } catch (InterruptedException e1) {
                                  // TODO Auto-generated catch block
                                  e1.printStackTrace();
                              }
                                
                            if (RecConn.getLocalCollection().debug)
                                System.out.println("--SERVER-- Attesa lettura pacchetto " + i);
                            
                            n++;
                        }
                        
                        n = i;
                        
                        //##SE LA LETTURA NON E' ANTATA BUON FINE, LEGGI IL PRIMO PACCHETTO VALIDO SUBITO PRECEDENTE A QUELLO RICHIESTO (i)
                        if (RecConn.getLocalCollection().read(n) == null || RecConn.getLocalCollection().read(n).length <= 0)
                        {
                            while (n >= RecConn.getLocalCollection().getFirstSnReceived() && (RecConn.getLocalCollection().read(n)== null || RecConn.getLocalCollection().read(n).length == 0))
                                n--;
                            
                            //##SE NON NE E' STATO TROVATO UNO PRIMA DI AVER RAGGIUNTO L'INIZIO DELLA COLLEZIONE, GENERA UN PACCHETTO VUOTO
                            if (n < RecConn.getLocalCollection().getFirstSnReceived())
                            {    
                                n = i;
                                byte[] emptyPkt = new byte[RecConn.getLocalCollection().getPktSize()];
                                for(int s = 0; s <= RecConn.getLocalCollection().getPktSize()-1; s++)
                                {
                                    emptyPkt[s] = 0;
                                }
                                RecConn.getLocalCollection().recover(n, emptyPkt);
                            }
                        }
                            
                            
                            
                       temp = RecConn.getLocalCollection().read(n);
                        
                       //##AGGIUNGI IL CONTENUTO DEL PACCHETTO ALL'ARRAY DI BYTE DA INVIARE AL THREAD CLIENT DELL'INTERLOCUTORE CHE HA FORMULATO LA RICHIESTA 
//                        if (temp != null)
//                        {
                        try {
                            System.arraycopy (temp, 0, send, (totalePkt-1)*pktSize, temp.length);
                        } catch (NullPointerException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            if (RecConn.getLocalCollection().debug)
                                System.out.println("pacchetto " + i + "non ancora inserito nella localCollection");
                        }
                        
                        totalePkt++;
//                        }
                      
                      //##DEBUG  
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
                
                //##INVIO DELL'ARRAY SEND
                if (!endServerThread) 
                {
                    try {
                        if (RecConn.getClientSocket().isConnected())
                        {
                        if (RecConn.getLocalCollection().debug)
                            System.out
                                    .println("--SERVER-- tentativo invio pacchetti richiesti ");

                        dos.write(send, 0, (totalePkt - 1) * pktSize);
                        dos.flush();

                        if (RecConn.getLocalCollection().debug)
                            System.out.println("--SERVER-- pacchetti INVIATI");

                        lastQueryByte = null;
                        }
                        else if (RecConn.getLocalCollection().debug)
                        {
                            System.out.println("--SERVER-- CONNESSIONE RECOVERY ASSENTE");
                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                
            }
            
        }//end while (!lengthRead || !queryRead)
		
        //##CHIUSURA DEGLI STREAM TRA SERVER LOCALE E CLIENT REMOTO
        try {
            if (RecConn.getLocalCollection().debug)
                System.out.println("_____________________________RecoveryServerThread: chiusura stream sul socket del client");
            RecConn.getClientSocket().getInputStream().close();//interrompe la connessione (anche l'outputStream viene chiuso)
            //RecConn.getClientSocket().getOutputStream().close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        System.out.println("--RECOVERY-- RecoveryServerThread END");
		
	}
	
	
	private static byte[] arrayResize(byte[] b, int newSize)
	{
	    byte[] newArray = new byte[newSize];
        System.arraycopy (b, 0, newArray, 0, b.length);
        return newArray;
	}
}