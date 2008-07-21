package pdcmvoice.recovery;

import java.io.*;

import java.util.StringTokenizer;
import pdcmvoice.impl.*;
import javax.sound.sampled.*;
import javax.sound.sampled.spi.*;

/**
 * 
 * @author Antonio
 */
public class RecoveryClientThread extends Thread
{
    private RecoveryConnection RecConn;
    private String lastQuery;
    private boolean lastQueryDone;
    //private RecoveryServerThread server; //NO! il serverThread si ferma da solo quando legge lastQuery == "END OF QUERY"!!! - serve per interrompere l'esecuzione del ServerThread
    private boolean stopQuery;
    
    public RecoveryClientThread(RecoveryConnection RecConn)
	{
        this.RecConn = RecConn;
        lastQuery = null;
        lastQueryDone = false;
        stopQuery = false;
	}
	
    //GESTIRE LA VITA DEL THREAD (ciclo while sullo stato della sessione rtp, ad esempio, ) e degli Stream e dei socket
    //ATTENZIONE AD ASPETTARE UN PO' PRIMA DI INIZIARE A FARE LE RICHIESTE DI RECOVERY!!
	public void run()
	{
		//deve scrivere con writeUTF oppure con writeBytes di DataOutputStream e concludere la stringa di richiesta con \n
	    //deve leggere con read(byte[] b, int off, int len)  di DataInputStream in base alla richiesta effettuata
	    
	    
	    /*
	     * RICORDA LA RIGA DI DEBUG
	     * 
	     * if (frame != null && SN % 10 != 0) //simulo perdita di 1 pacchetto ogni 10
	     * 
	     * in Decoder.java
	     */
	    
	    DataInputStream dis = null;
        DataOutputStream dos = null;
        try {
            dis = new DataInputStream(RecConn.getServerSocket().getInputStream());
            dos = new DataOutputStream(RecConn.getServerSocket().getOutputStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
	    
	    int pktSize = RecConn.getRemoteCollection().getPktSize();
        byte[] temp = new byte[pktSize];
        boolean rtpDown;
        
        try {
            Thread.sleep(2000); //attesa prima di partire
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        int writingTest = 1;
        
        //TODO
        //CONTROLLARE PRIMA LA QUANTITA' DI PACCHETTI RICHIESTI DALLA QUERY!!! SE TROPPO POCHI; ASPETTA A MANDARLA
        while(!stopQuery)
        {
            
            try {
                Thread.sleep(1000); //attesa durante la ricezione
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            
            rtpDown= RecConn.getRtpSession().isEnding() || RecConn.getRtpSession() == null;
            
            if (!rtpDown)
                lastQuery = RecConn.getRemoteCollection().findHoles(0, false);
            else
                lastQuery = RecConn.getRemoteCollection().findAllHoles();
            
            if (lastQuery != "")
            {
                if (RecConn.getRemoteCollection().debug)
                    System.out.println("INVIO QUERY: " + lastQuery);
                try {
                    dos.writeBytes(lastQuery + "\n");
                    dos.flush();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            else if (RecConn.getRemoteCollection().debug)
                System.out.println("ClientThread: nessun pkt perso dall'ultima ricerca");
           
            StringTokenizer izer = new StringTokenizer(lastQuery, ";", false);
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
                    try {
                    dis.read(temp, 0, pktSize);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    RecConn.getLocalCollection().recover(i, temp);
                    
                    if (RecConn.getRemoteCollection().debug)
                        System.out.println("pkt recuperato dal ClientThread: " + i);
                   
                }
                
            }
            
            lastQuery = null;
            
            if (rtpDown) stopQuery = true;
            
            //ATTENZIONE!!
            //PROVA SCRITTURA DELL'AUDIO RICEVUTO FINO ALLA SECONDA QUERY
            if (writingTest++ >= 2)
                stopQuery = true;
        }
        
        //informa il server dell'altro interlocutore che sono stati ricevuti tutti i pacchetti
        try {
            dos.writeBytes("END OF QUERY\n");
            dos.flush();
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        /* 
        ELABORAZIONE DELLE COLLEZIONI
                
         
         */
        
        RecoveryCollection local = RecConn.getLocalCollection();
        RecoveryCollection remote = RecConn.getRemoteCollection();
            
        Decoder localDecoder = new Decoder (local.getEncodedFormat());
        Decoder remoteDecoder = new Decoder (remote.getEncodedFormat());
        
        AudioInputStream localAis = null;
        AudioInputStream remoteAis = null;
        
        try {
             localAis = localDecoder.getAudioInputStream();
             remoteAis = remoteDecoder.getAudioInputStream();
        } catch (UnsupportedAudioFileException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        int localPktSize = local.getPktSize();
        int remotePktSize = remote.getPktSize();
        
        byte[] localTemp = new byte[320];
        byte[] remoteTemp = new byte[320];
        
        int localSn;
        int remoteSn;
        
        
        byte[] localArray = new byte[160000]; //50 pkt/s da 320Byte ciascuno per 10 secondi
        byte[] remoteArray = new byte[160000]; //50 pkt/s da 320Byte ciascuno per 10 secondi
        
        //creo i due input stream e i due array da scrivere nei file
        int i = 0;
        for (; ; i++)
        {
            localSn = local.getFirstSnReceived() + i;
           
            if (i*320 >= localArray.length - 1)
                arrayResize(localArray, 2 * localArray.length);
            
            if (local.read(localSn) != null)
            {
                //localDecoder.decodeFrame(local.read(localSn), localSn, 0);
                localDecoder.decodeFrame(local.read(localSn));
                
                try {
                    localAis.read(localArray, i*320 , 320);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
            }
            else
                break;
        }
        
        int j = 0;
        for (; ; j++)
        {
            remoteSn = remote.getFirstSnReceived() + j;
           
            if (j*320 >= remoteArray.length - 1)
                arrayResize(remoteArray, 2 * remoteArray.length);
            
            if (remote.read(remoteSn) != null)
            {
                //remoteDecoder.decodeFrame(remote.read(remoteSn), remoteSn, 0);
                remoteDecoder.decodeFrame(remote.read(remoteSn));
                
                try {
                    remoteAis.read(remoteArray, j*320 , 320);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            else
                break;
        }
        
        //ora localAis e remoteAis contengono gli stream decodificati (PCM)
        
        //scrittura nei due file .wav ///DEVO USARE UN MIXER!!
        FileOutputStream fos1; 
        DataOutputStream dos1;
        
        

        try {

          File file= new File("C:\\local.wav");
          fos1 = new FileOutputStream(file);
          dos1=new DataOutputStream(fos1);
          dos1.write(localArray, 0, (i-1)*320);
          dos1.flush();
          
          fos1.close();
          dos1.close();
          
        } catch (IOException e) {
          e.printStackTrace();
        }
        
        try {

            File file= new File("C:\\remote.wav");
            fos1 = new FileOutputStream(file);
            dos1=new DataOutputStream(fos1);
            dos1.write(localArray, 0, (j-1)*320);
            dos1.flush();
            fos1.close();
            dos1.close();
            
          } catch (IOException e) {
            e.printStackTrace();
          }
        
        /* PROVA DI MIXAGGIO
        byte[] outputCollection = new byte[160000]; //50 pkt/s da 320Byte ciascuno per 10 secondi
        
        //i pacchetti decompressi sono sempre di 320Byte e vengono inseriti nell'input stream restituito dal decoder
        int max = 0;
        if (local.getLastSnReceived() >= remote.getLastSnReceived())
            max = local.getLastSnReceived();
        else
            max = remote.getLastSnReceived();
        
        
        int i = 0;
        for (; i <= max; i++)
        {
            localSn = local.getFirstSnReceived() + i;
            remoteSn = remote.getFirstSnReceived() + i;
            
            if (i*320 >= outputCollection.length - 1)
                arrayResize(outputCollection, 2 * outputCollection.length);
            
            if (local.read(localSn) != null && remote.read(remoteSn) != null)
            {
                localDecoder.decodeFrame(local.read(localSn), localSn, 0);
                remoteDecoder.decodeFrame(remote.read(remoteSn), remoteSn, 0);
                
                for (int j = 0; j <= 319; j++)
                {
                    
                    try {
                        //in localAis e in remoteAis ci sono, via via, gli stream di pacchetti non compressi di dimensione 320Byte
                        localAis.read(localTemp, 0, 320);
                        remoteAis.read(remoteTemp, 0, 320);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                    outputCollection[i+j] = (byte)(localTemp[j] + remoteTemp[j]);
                }
                
                
            }
            else if (local.read(localSn) != null)
            {
                for (int j = 0; j <= 319; j++)
                {
                    outputCollection[i+j] = localTemp[j];
                }
            }
            else if (remote.read(remoteSn) != null)
            {
                for (int j = 0; j <= 319; j++)
                {
                    outputCollection[i+j] = remoteTemp[j];
                }
            }
            else break;
                
            
        
        }
        
        
        //outputCollection[] contiene i byte del mixaggio; i*320 sono tutti i byte del mixaggio
        //ora bisogna salvare tutto su file
        AudioFormat mixFormat = localAis.getFormat();
        */
        
        
        
        
        
	}
	
	private static byte[] arrayResize(byte[] b, int newSize)
    {
        byte[] newArray = new byte[newSize];
        System.arraycopy (b, 0, newArray, 0, b.length);
        return newArray;
    }
	
}