package pdcmvoice.recovery;


import java.io.*;

import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.List;
import pdcmvoice.impl.*;
import javax.sound.sampled.*;
import javax.sound.sampled.spi.*;
import org.xiph.speex.spi.*;

/**
 * 
 * @author Antonio
 */
public class CopyOfRecoveryClientThreadProvaConcatenazioneAis extends Thread
{
    private RecoveryConnection RecConn;
    private String lastQuery;
    private boolean lastQueryDone;
    //private RecoveryServerThread server; //NO! il serverThread si ferma da solo quando legge lastQuery == "END OF QUERY"!!! - serve per interrompere l'esecuzione del ServerThread
    private boolean stopQuery;
    
    public CopyOfRecoveryClientThreadProvaConcatenazioneAis(RecoveryConnection RecConn)
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
                    RecConn.getRemoteCollection().recover(i, temp, false);
                    
                    if (RecConn.getRemoteCollection().debug)
                        System.out.println("pkt recuperato dal ClientThread: " + i);
                   
                }
                
            }
            
            lastQuery = null;
            
            if (rtpDown) stopQuery = true;
            
            //ATTENZIONE!!
            //PROVA SCRITTURA DELL'AUDIO RICEVUTO FINO ALLA QUARTA QUERY
            if (writingTest++ >= 4)
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
        
        
        //if (RecConn.getRemoteCollection().debug)
            System.out.println("--ELAB-- INIZIO ELABORAZIONE DELLE COLLEZIONI");
        
        RecoveryCollection local = RecConn.getLocalCollection();
        RecoveryCollection remote = RecConn.getRemoteCollection();
            
        //Decoder localDecoder = new Decoder (local.getEncodedFormat());
        Decoder localDecoder = new Decoder (local.getEncodedFormat(), true);
        localDecoder.init();
        Decoder remoteDecoder = new Decoder (remote.getEncodedFormat(), true);
        remoteDecoder.init();
        
        AudioInputStream localAis = null;
        AudioInputStream remoteAis = null;
        
        AudioInputStream localAisTemp = null;
        AudioInputStream remoteAisTemp = null;
        
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
        
        /*
        STRATEGIA 1
        Converto le collezioni in AudioInputStream di tipo Speex
        poi converto in AudioInputStream di tipo PCM con SpeexFormatConversionProvider
        poi scrivo nei file usando metodo write di SpeexAudioFileWriter
        
        STRATEGIA 2
        Converto i singoli frame (array di byte da 20 elementi) in frame PCM ottenendo un AudioInputStream PCM con Decoder.java (marco)
        "aggiorno" l'AudioInputStream assegnando la dimensione IN FRAME dello stream (num byte dello stream / num byte per frame)
        poi scrivo gli AudioInputStream su file con AudioSystem.write(..) dopo aver assegnato la dimensione
        
        Per migliorare le prestazioni e non dover usare un buffer troppo esteso conviene decodificare i pacchetti della collezione, leggere dagli ais e scrivere su file "a blocchi" 
        
        STRATEGIA 3
        a partire dagli stream OPPURE dai file .wav "separati" (utile per gestione delle lunghezze)
        preparazione degli stream per MixingAudioInputStream..come?? come "aggiornamento" della strategia 2??
        mixaggio con audioInputStream = new MixingAudioInputStream(audioFormat, audioInputStreamList);
        e scrittura su file
        
        
        
        
        */
        
        
       
/*
 * STRATEGIA 2
 */
//        
//      //if (RecConn.getLocalCollection().debug)
//        System.out.println("--ELAB-- ELABORAZIONE LOCAL COLLECTION");
//    
//        int i = 0;
//        for (; ; i++)
//        {
//            localSn = local.getFirstSnReceived() + i;
//           
//            //if (local.read(localSn) != null)
//            if ((local.lastSnReceived > 0 && localSn <= local.lastSnReceived)
//                    || local.read(localSn) != null)
//            {
//                //localDecoder.decodeFrame(local.read(localSn), localSn, 0);
//                localDecoder.decodeFrame(local.read(localSn));
//                
//                //if (RecConn.getLocalCollection().debug)
//                    System.out.print("--ELAB-- Decodificato Sn " + localSn + ": ");
//                    
//                    for (int l = 0; l <= local.read(localSn).length -1; l++)
//                    {
//                        System.out.print(local.read(localSn)[l] + " ");
//                    }
//                    
//                    System.out.println("");
//                
//            }
//            else
//            {
//              //if (RecConn.getLocalCollection().debug)
//                System.out.println("--ELAB-- Trovato pacchetto NULL");
//                break;
//            }
//        }
//        
//      //if (RecConn.getLocalCollection().debug)
//        {
//        System.out.println("--ELAB-- formato localAis: " + localAis.getFormat().toString());
//        try {
//            System.out.println("--ELAB-- byte presenti in localAis: " + localAis.available());
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        System.out.println("--ELAB-- lunghezza localAis in frame: " + localAis.getFrameLength());
//        }
//        
////        try {
//////            while (localAis.available() < 10)
//////            {
//////                Thread.sleep(50);
//////              //if (RecConn.getLocalCollection().debug)
//////                System.out.println("--ELAB-- attendo 50ms per l'AudioInputStream");
//////            }
////            int byteRead = localAis.read(localArray, 0, 320);
////             
////          //if (RecConn.getLocalCollection().debug)
////            {
////            System.out.print("--ELAB-- localArray: ");
////            for (int f = 0; f <= byteRead - 1; f++)
////                System.out.print(localArray[f] + ",");
////            System.out.println("");
////            }
////                
////            } catch (Exception e) {
////                // TODO: handle exception
////                e.printStackTrace();
////            }    
//        
//        
//        
//        //if (RecConn.getLocalCollection().debug)
//        {
//            AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes(localAis);  
//            for (int f = 0; f <= types.length - 1; f++)
//                System.out.print("--ELAB-- localAis types: " + types[f] + ",");
//            System.out.println("");
//        }
//        
//        //SCRITTURA DI localAis SU FILE .WAV
//        
//
////      AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes(toBeWritten);
////      //if (RecConn.getLocalCollection().debug)
////            for (int f = 0; f <= types.length - 1; f++)
////            System.out.print("--ELAB-- types: " + types[f] + ",");
//       
//        
//        //Aggiornamento lunghezza IN FRAME dello stream localAis (necessaria per la scrittura del file .wav)
//        try {
//            localAis = new AudioInputStream(localAis, localAis.getFormat(), localAis.available()/localAis.getFormat().getFrameSize());
//        } catch (IOException e2) {
//            // TODO Auto-generated catch block
//            e2.printStackTrace();
//        }
//        
//        //visualizzazione info sul NUOVO localAis
//      //if (RecConn.getLocalCollection().debug)
//        {
//        System.out.println("--ELAB-- AGG. formato localAis: " + localAis.getFormat().toString());
//        try {
//            System.out.println("--ELAB-- AGG. byte presenti in localAis: " + localAis.available());
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        System.out.println("--ELAB-- AGG. lunghezza localAis in frame: " + localAis.getFrameLength());
//        System.out.println("--ELAB-- AGG. frame size localAis in byte: " + localAis.getFormat().getFrameSize());
//        }
//      
////SCRITTURA
//        
////        if (RecConn.getLocalCollection().debug)
////        {
////            File file= new File("F:\\local.wav");
////          //if (RecConn.getLocalCollection().debug)
////            System.out.println("--ELAB-- File aperto");
////            
////            if (!file.canWrite())
////              //if (RecConn.getLocalCollection().debug)
////                System.out.println("--ELAB-- ATTENZIONE! Non � possibile scrivere nel file.");
////            
////            FileOutputStream fos = null;
////            try {
////                fos = new FileOutputStream(file);
////            } catch (FileNotFoundException e1) {
////                // TODO Auto-generated catch block
////                e1.printStackTrace();
////            }
////            
////            int byteWritten = 0;
////            try {
////              //if (RecConn.getLocalCollection().debug)
////                System.out.println("--ELAB-- Inizio scrittura del file");
////                
////                byteWritten = AudioSystem.write(localAis, AudioFileFormat.Type.WAVE, fos);
////                
////                fos.flush();
////                fos.close();
////                } catch (IOException e) {
////                    // TODO Auto-generated catch block
////                    e.printStackTrace();
////                }
////           
////            //if (RecConn.getLocalCollection().debug)
////            System.out.println("--ELAB-- Scritti " + byteWritten + " byte nel file .wave");
////        }
//        
//       
//        
//        
//        
//        
//        //REMOTE COLLECTION OLD
//        
//        /*
//        if (RecConn.getRemoteCollection().debug)
//            System.out.println("--ELAB-- ELABORAZIONE REMOTE COLLECTION");
//        
//        int j = 0;
//        for (; ; j++)
//        {
//            remoteSn = remote.getFirstSnReceived() + j;
//           
//            if (remote.read(remoteSn) != null)
//            {
//                //remoteDecoder.decodeFrame(remote.read(remoteSn), remoteSn, 0);
//                remoteDecoder.decodeFrame(remote.read(remoteSn));
//                
//                if (RecConn.getRemoteCollection().debug)
//                    System.out.println("--ELAB-- Decodificato Sn " + remoteSn);
//                
//            }
//            else
//                break;
//        }
//        
//        */
//        
//      //if (RecConn.getRemoteCollection().debug)
//        System.out.println("--ELAB-- ELABORAZIONE REMOTE COLLECTION");
//    
//        i = 0;
//        for (; ; i++)
//        {
//            remoteSn = remote.getFirstSnReceived() + i;
//           
//            //if (remote.read(remoteSn) != null)
//            if ((remote.lastSnReceived > 0 && remoteSn <= remote.lastSnReceived)
//                    || remote.read(remoteSn) != null)
//            {
//                //remoteDecoder.decodeFrame(local.read(remoteSn), remoteSn, 0);
//                remoteDecoder.decodeFrame(remote.read(remoteSn));
//                
//                //if (RecConn.getRemoteCollection().debug)
//                    System.out.print("--ELAB-- Decodificato Sn " + remoteSn + ": ");
//                    
//                    for (int l = 0; l <= remote.read(remoteSn).length -1; l++)
//                    {
//                        System.out.print(remote.read(remoteSn)[l] + " ");
//                    }
//                    
//                    System.out.println("");
//                
//            }
//            else
//            {
//              //if (RecConn.getRemoteCollection().debug)
//                System.out.println("--ELAB-- Trovato pacchetto NULL");
//                break;
//            }
//        }
//        
//      //if (RecConn.getRemoteCollection().debug)
//        {
//        System.out.println("--ELAB-- formato remoteAis: " + remoteAis.getFormat().toString());
//        try {
//            System.out.println("--ELAB-- byte presenti in remoteAis: " + remoteAis.available());
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        System.out.println("--ELAB-- lunghezza remoteAis in frame: " + remoteAis.getFrameLength());
//        }
//        
//        
//        //if (RecConn.getRemoteCollection().debug)
//        {
//            AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes(remoteAis);  
//            for (int f = 0; f <= types.length - 1; f++)
//                System.out.print("--ELAB-- remoteAis types: " + types[f] + ",");
//            System.out.println("");
//        }
//        
////SCRITTURA DI localAis SU FILE .WAV
//        
//
////      AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes(toBeWritten);
////      //if (RecConn.getLocalCollection().debug)
////            for (int f = 0; f <= types.length - 1; f++)
////            System.out.print("--ELAB-- types: " + types[f] + ",");
//       
//        
//        //Aggiornamento lunghezza IN FRAME dello stream remoteAis (necessaria per la scrittura del file .wav)
//        try {
//            remoteAis = new AudioInputStream(remoteAis, remoteAis.getFormat(), remoteAis.available()/remoteAis.getFormat().getFrameSize());
//        } catch (IOException e2) {
//            // TODO Auto-generated catch block
//            e2.printStackTrace();
//        }
//        
//        //visualizzazione info sul NUOVO localAis
//      //if (RecConn.getRemoteCollection().debug)
//        {
//        System.out.println("--ELAB-- AGG. formato remoteAis: " + remoteAis.getFormat().toString());
//        try {
//            System.out.println("--ELAB-- AGG. byte presenti in remoteAis: " + remoteAis.available());
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        System.out.println("--ELAB-- AGG. lunghezza remoteAis in frame: " + remoteAis.getFrameLength());
//        System.out.println("--ELAB-- AGG. frame size remoteAis in byte: " + remoteAis.getFormat().getFrameSize());
//        }
//      
////SCRITTURA
////      if (RecConn.getRemoteCollection().debug)
////      {
////            File file= new File("F:\\remote.wav");
////          //if (RecConn.getLocalCollection().debug)
////            System.out.println("--ELAB-- File aperto");
////            
////            if (!file.canWrite())
////              //if (RecConn.getLocalCollection().debug)
////                System.out.println("--ELAB-- ATTENZIONE! Non è possibile scrivere nel file.");
////            
////            FileOutputStream fos = null;
////            try {
////                fos = new FileOutputStream(file);
////            } catch (FileNotFoundException e1) {
////                // TODO Auto-generated catch block
////                e1.printStackTrace();
////            }
////            
////            int byteWritten = 0;
////            try {
////              //if (RecConn.getLocalCollection().debug)
////                System.out.println("--ELAB-- Inizio scrittura del file");
////                
////                byteWritten = AudioSystem.write(remoteAis, AudioFileFormat.Type.WAVE, fos);
////                
////                fos.flush();
////                fos.close();
////                } catch (IOException e) {
////                    // TODO Auto-generated catch block
////                    e.printStackTrace();
////                }
////           
////            //if (RecConn.getLocalCollection().debug)
////            System.out.println("--ELAB-- Scritti " + byteWritten + " byte nel file .wave");
////        
////      }
//        
//        List collectionAisList = new ArrayList();
//        collectionAisList.add(localAis);
//        collectionAisList.add(remoteAis);
//        
//        AudioInputStream mixedAis = new MixingAudioInputStream(localAis.getFormat(), collectionAisList);
//        
//      //if (RecConn.getRemoteCollection().debug)
//        {
//        System.out.println("--ELAB-- formato mixedAis: " + mixedAis.getFormat().toString());
//        try {
//            System.out.println("--ELAB-- byte presenti in mixedAis: " + mixedAis.available());
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        System.out.println("--ELAB-- lunghezza mixedAis in frame: " + mixedAis.getFrameLength());
//        }
//        
//        
//        //if (RecConn.getRemoteCollection().debug)
//        {
//            AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes(mixedAis);  
//            for (int f = 0; f <= types.length - 1; f++)
//                System.out.print("--ELAB-- mixedAis types: " + types[f] + ",");
//            System.out.println("");
//        }
//        
//        
//      //Aggiornamento lunghezza IN FRAME dello stream mixedAis (necessaria per la scrittura del file .wav)
//        try {
//            remoteAis = new AudioInputStream(mixedAis, mixedAis.getFormat(), mixedAis.available()/mixedAis.getFormat().getFrameSize());
//           //mixedAis= new AudioInputStream(mixedAis, mixedAis.getFormat(), mixedAis.available()/mixedAis.getFormat().getFrameSize());
//        } catch (IOException e2) {
//            // TODO Auto-generated catch block
//            e2.printStackTrace();
//        }
//        
//        //visualizzazione info sul NUOVO mixedAis
//      //if (RecConn.getRemoteCollection().debug)
//        {
//        System.out.println("--ELAB-- AGG. formato mixedAis: " + mixedAis.getFormat().toString());
//        try {
//            System.out.println("--ELAB-- AGG. byte presenti in mixedAis: " + mixedAis.available());
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        System.out.println("--ELAB-- AGG. lunghezza mixedAis in frame: " + mixedAis.getFrameLength());
//        System.out.println("--ELAB-- AGG. frame size mixedAis in byte: " + mixedAis.getFormat().getFrameSize());
//        }
//        
//        
//        
////SCRITTURA del mixedAis
//                
//                File file= new File("F:\\mixed.wav");
//              //if (RecConn.getLocalCollection().debug)
//                System.out.println("--ELAB-- File aperto");
//                
//                if (!file.canWrite())
//                  //if (RecConn.getLocalCollection().debug)
//                    System.out.println("--ELAB-- ATTENZIONE! Non è possibile scrivere nel file.");
//                
//                FileOutputStream fos = null;
//                try {
//                    fos = new FileOutputStream(file);
//                } catch (FileNotFoundException e1) {
//                    // TODO Auto-generated catch block
//                    e1.printStackTrace();
//                }
//                
//                int byteWritten = 0;
//                try {
//                  //if (RecConn.getLocalCollection().debug)
//                    System.out.println("--ELAB-- Inizio scrittura del file");
//                    
//                    byteWritten = AudioSystem.write(mixedAis, AudioFileFormat.Type.WAVE, fos);
//                    
//                    fos.flush();
//                    fos.close();
//                    } catch (IOException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//               
//                //if (RecConn.getLocalCollection().debug)
//                System.out.println("--ELAB-- Scritti " + byteWritten + " byte nel file .wave");
                
 
        
/* -----------------------
 * STRATEGIA 2 MIGLIORATA
 * -----------------------
 */
  
        //PREDISPOSIZIONE STREAM E FILE
      
        //LOCAL AIS
      //if (RecConn.getLocalCollection().debug)
        {
        System.out.println("--ELAB-- formato localAis: " + localAis.getFormat().toString());
        try {
            System.out.println("--ELAB-- byte presenti in localAis: " + localAis.available());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("--ELAB-- lunghezza localAis in frame: " + localAis.getFrameLength());
        }

        
        //if (RecConn.getLocalCollection().debug)
        {
            AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes(localAis);  
            for (int f = 0; f <= types.length - 1; f++)
                System.out.print("--ELAB-- localAis types: " + types[f] + ",");
            System.out.println("");
        }
        
        //SCRITTURA DI localAis SU FILE .WAV
        

//      AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes(toBeWritten);
//      //if (RecConn.getLocalCollection().debug)
//            for (int f = 0; f <= types.length - 1; f++)
//            System.out.print("--ELAB-- types: " + types[f] + ",");
       
        
        
        
        //REMOTE AIS
        
      //if (RecConn.getRemoteCollection().debug)
        {
        System.out.println("--ELAB-- formato remoteAis: " + remoteAis.getFormat().toString());
        try {
            System.out.println("--ELAB-- byte presenti in remoteAis: " + remoteAis.available());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("--ELAB-- lunghezza remoteAis in frame: " + remoteAis.getFrameLength());
        }
        
        
        //if (RecConn.getRemoteCollection().debug)
        {
            AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes(remoteAis);  
            for (int f = 0; f <= types.length - 1; f++)
                System.out.print("--ELAB-- remoteAis types: " + types[f] + ",");
            System.out.println("");
        }
        
//SCRITTURA DI localAis SU FILE .WAV
        

//      AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes(toBeWritten);
//      //if (RecConn.getLocalCollection().debug)
//            for (int f = 0; f <= types.length - 1; f++)
//            System.out.print("--ELAB-- types: " + types[f] + ",");
       
        
        
        
        //MIXED AIS
        
        List collectionAisList = new ArrayList();
        
        AudioInputStream mixedAis = null;
        
        //FILE
        
        File file= new File("F:\\mixed.wav");
        //if (RecConn.getLocalCollection().debug)
          System.out.println("--ELAB-- File aperto");
          
          if (!file.canWrite())
            //if (RecConn.getLocalCollection().debug)
              System.out.println("--ELAB-- ATTENZIONE! Non e' possibile scrivere nel file.");
          
          FileOutputStream fos = null;
          try {
              fos = new FileOutputStream(file);
          } catch (FileNotFoundException e1) {
              // TODO Auto-generated catch block
              e1.printStackTrace();
          }
          
          
          //DECODIFICA PACCHETTI E SCRITTURA: TUTTO "A BLOCCHI"
          
          localSn = local.getFirstSnReceived();//Sn da cui iniziare a decodificare il prox blocco di frame
          remoteSn = remote.getFirstSnReceived();
          
          boolean localCondition = (local.lastSnReceived > 0 && localSn <= local.lastSnReceived)
                                      || local.read(localSn) != null; //utile per i test
          boolean remoteCondition = (remote.lastSnReceived > 0 && remoteSn <= remote.lastSnReceived)
                                      || remote.read(remoteSn) != null; //utile per i test
          int localPkt = 100;
          int remotePkt = 100;
          
          
          int i = 0;
          
          while (((local.lastSnReceived > 0 && localSn <= local.lastSnReceived)
                      || local.read(localSn) != null) 
                || 
                  ((remote.lastSnReceived > 0 && remoteSn <= remote.lastSnReceived)
                      || remote.read(remoteSn) != null))
          {
              //if (RecConn.getLocalCollection().debug)
                System.out.println("--ELAB-- ELABORAZIONE LOCAL COLLECTION");
            
                i = localSn;
                for (; i <= localSn + 100; i++)
                {
                    //DECODIFICO ANCHE EVENTUALI PACCHETTI NULLI!
                    if (local.read(i) != null) 
                    {
                        //localDecoder.decodeFrame(local.read(localSn), localSn, 0);
                        localDecoder.decodeFrame(local.read(i));
                        
                        //if (RecConn.getLocalCollection().debug)
                            System.out.print("--ELAB-- Decodificato Sn " + i + ": ");
                            
                            for (int l = 0; l <= local.read(i).length -1; l++)
                            {
                                System.out.print(local.read(i)[l] + " ");
                            }
                            
                            System.out.println("");
                        
                    }
                    else
                    //if (local.read(i) == null)
                    {
                      //if (RecConn.getLocalCollection().debug)
                        System.out.println("--ELAB-- Trovato pacchetto NULL: Sn " + localSn);
                        break;
                    }
                }
                localSn = i; //i gia' incrementato dal for che non ha verificato la collezione
                
              //if (RecConn.getLocalCollection().debug)
                {
                System.out.println("--ELAB-- formato localAis: " + localAis.getFormat().toString());
                try {
                    System.out.println("--ELAB-- byte presenti in localAis: " + localAis.available());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                System.out.println("--ELAB-- lunghezza localAis in frame: " + localAis.getFrameLength());
                }
                
              //Aggiornamento lunghezza IN FRAME dello stream localAis (necessaria per la scrittura del file .wav)
                try {
                    localAisTemp = new AudioInputStream(localAis, localAis.getFormat(), localAis.available()/localAis.getFormat().getFrameSize());
                } catch (IOException e2) {
                    // TODO Auto-generated catch block
                    e2.printStackTrace();
                }
                
                //visualizzazione info sul NUOVO localAis
              //if (RecConn.getLocalCollection().debug)
                {
                System.out.println("--ELAB-- AGG. formato localAisTemp: " + localAisTemp.getFormat().toString());
                try {
                    System.out.println("--ELAB-- AGG. byte presenti in localAisTemp: " + localAisTemp.available());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                System.out.println("--ELAB-- AGG. lunghezza localAisTemp in frame: " + localAisTemp.getFrameLength());
                System.out.println("--ELAB-- AGG. frame size localAisTemp in byte: " + localAisTemp.getFormat().getFrameSize());
                }
              
        //SCRITTURA
                
//                if (RecConn.getLocalCollection().debug)
//                {
//                    File file= new File("F:\\local.wav");
//                  //if (RecConn.getLocalCollection().debug)
//                    System.out.println("--ELAB-- File aperto");
//                    
//                    if (!file.canWrite())
//                      //if (RecConn.getLocalCollection().debug)
//                        System.out.println("--ELAB-- ATTENZIONE! Non � possibile scrivere nel file.");
//                    
//                    FileOutputStream fos = null;
//                    try {
//                        fos = new FileOutputStream(file);
//                    } catch (FileNotFoundException e1) {
//                        // TODO Auto-generated catch block
//                        e1.printStackTrace();
//                    }
//                    
//                    int byteWritten = 0;
//                    try {
//                      //if (RecConn.getLocalCollection().debug)
//                        System.out.println("--ELAB-- Inizio scrittura del file");
//                        
//                        byteWritten = AudioSystem.write(localAis, AudioFileFormat.Type.WAVE, fos);
//                        
//                        fos.flush();
//                        fos.close();
//                        } catch (IOException e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        }
//                   
//                    //if (RecConn.getLocalCollection().debug)
//                    System.out.println("--ELAB-- Scritti " + byteWritten + " byte nel file .wave");
//                }
                
               
                
                
                
                
                //REMOTE COLLECTION OLD
                
                /*
                if (RecConn.getRemoteCollection().debug)
                    System.out.println("--ELAB-- ELABORAZIONE REMOTE COLLECTION");
                
                int j = 0;
                for (; ; j++)
                {
                    remoteSn = remote.getFirstSnReceived() + j;
                   
                    if (remote.read(remoteSn) != null)
                    {
                        //remoteDecoder.decodeFrame(remote.read(remoteSn), remoteSn, 0);
                        remoteDecoder.decodeFrame(remote.read(remoteSn));
                        
                        if (RecConn.getRemoteCollection().debug)
                            System.out.println("--ELAB-- Decodificato Sn " + remoteSn);
                        
                    }
                    else
                        break;
                }
                
                */
                
              //if (RecConn.getRemoteCollection().debug)
                System.out.println("--ELAB-- ELABORAZIONE REMOTE COLLECTION");
            
                i = remoteSn;
                for (; i <= remoteSn + 100; i++)
                {
                    
                   
                    if (remote.read(i) != null)
                    {
                        //remoteDecoder.decodeFrame(local.read(remoteSn), remoteSn, 0);
                        remoteDecoder.decodeFrame(remote.read(i));
                        
                        //if (RecConn.getRemoteCollection().debug)
                            System.out.print("--ELAB-- Decodificato Sn " + i + ": ");
                            
                            for (int l = 0; l <= remote.read(i).length -1; l++)
                            {
                                System.out.print(remote.read(i)[l] + " ");
                            }
                            
                            System.out.println("");
                        
                    }
                    else
                    //if (remote.read(i) == null)
                    {
                      //if (RecConn.getRemoteCollection().debug)
                        System.out.println("--ELAB-- Trovato pacchetto NULL");
                        break;
                    }
                }
                
                remoteSn = i;
              
              //if (RecConn.getRemoteCollection().debug)
                {
                System.out.println("--ELAB-- formato remoteAis: " + remoteAis.getFormat().toString());
                try {
                    System.out.println("--ELAB-- byte presenti in remoteAis: " + remoteAis.available());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                System.out.println("--ELAB-- lunghezza remoteAis in frame: " + remoteAis.getFrameLength());
                }
                
              //Aggiornamento lunghezza IN FRAME dello stream remoteAis (necessaria per la scrittura del file .wav)
                try {
                    remoteAisTemp = new AudioInputStream(remoteAis, remoteAis.getFormat(), remoteAis.available()/remoteAis.getFormat().getFrameSize());
                } catch (IOException e2) {
                    // TODO Auto-generated catch block
                    e2.printStackTrace();
                }
                
                //visualizzazione info sul NUOVO remoteAis
              //if (RecConn.getRemoteCollection().debug)
                {
                System.out.println("--ELAB-- AGG. formato remoteAisTemp: " + remoteAisTemp.getFormat().toString());
                try {
                    System.out.println("--ELAB-- AGG. byte presenti in remoteAisTemp: " + remoteAisTemp.available());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                System.out.println("--ELAB-- AGG. lunghezza remoteAisTemp in frame: " + remoteAisTemp.getFrameLength());
                System.out.println("--ELAB-- AGG. frame size remoteAisTemp in byte: " + remoteAisTemp.getFormat().getFrameSize());
                }
              
        //SCRITTURA
//              if (RecConn.getRemoteCollection().debug)
//              {
//                    File file= new File("F:\\remote.wav");
//                  //if (RecConn.getLocalCollection().debug)
//                    System.out.println("--ELAB-- File aperto");
//                    
//                    if (!file.canWrite())
//                      //if (RecConn.getLocalCollection().debug)
//                        System.out.println("--ELAB-- ATTENZIONE! Non è possibile scrivere nel file.");
//                    
//                    FileOutputStream fos = null;
//                    try {
//                        fos = new FileOutputStream(file);
//                    } catch (FileNotFoundException e1) {
//                        // TODO Auto-generated catch block
//                        e1.printStackTrace();
//                    }
//                    
//                    int byteWritten = 0;
//                    try {
//                      //if (RecConn.getLocalCollection().debug)
//                        System.out.println("--ELAB-- Inizio scrittura del file");
//                        
//                        byteWritten = AudioSystem.write(remoteAis, AudioFileFormat.Type.WAVE, fos);
//                        
//                        fos.flush();
//                        fos.close();
//                        } catch (IOException e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        }
//                   
//                    //if (RecConn.getLocalCollection().debug)
//                    System.out.println("--ELAB-- Scritti " + byteWritten + " byte nel file .wave");
//                
//              }
                
                
        //PREPARAZIONE DEL mixedAis
                mixedAis = new MixingAudioInputStream(localAis.getFormat(), collectionAisList);
                collectionAisList.add(localAisTemp);
                collectionAisList.add(remoteAisTemp);
                
                
                
              //if (RecConn.getRemoteCollection().debug)
                {
                System.out.println("--ELAB-- formato mixedAis: " + mixedAis.getFormat().toString());
                try {
                    System.out.println("--ELAB-- byte presenti in mixedAis: " + mixedAis.available());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                System.out.println("--ELAB-- lunghezza mixedAis in frame: " + mixedAis.getFrameLength());
                }
                
                
                //if (RecConn.getRemoteCollection().debug)
                {
                    AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes(mixedAis);  
                    for (int f = 0; f <= types.length - 1; f++)
                        System.out.print("--ELAB-- mixedAis types: " + types[f] + ",");
                    System.out.println("");
                }
                
                
//              //Aggiornamento lunghezza IN FRAME dello stream mixedAis (necessaria per la scrittura del file .wav)
//                try {
//                    mixedAis = new AudioInputStream(mixedAis, mixedAis.getFormat(), mixedAis.available()/mixedAis.getFormat().getFrameSize());
//                } catch (IOException e2) {
//                    // TODO Auto-generated catch block
//                    e2.printStackTrace();
//                }
                
                //visualizzazione info sul NUOVO mixedAis
              //if (RecConn.getRemoteCollection().debug)
                {
                System.out.println("--ELAB-- AGG. formato mixedAis: " + mixedAis.getFormat().toString());
                try {
                    System.out.println("--ELAB-- AGG. byte presenti in mixedAis: " + mixedAis.available());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                System.out.println("--ELAB-- AGG. lunghezza mixedAis in frame: " + mixedAis.getFrameLength());
                System.out.println("--ELAB-- AGG. frame size mixedAis in byte: " + mixedAis.getFormat().getFrameSize());
                }
                
                
        //SCRITTURA del mixedAis
                        
                int byteWritten = 0;
                try {
                  //if (RecConn.getLocalCollection().debug)
                    System.out.println("--ELAB-- Inizio scrittura del file");
                    
                    byteWritten = AudioSystem.write(mixedAis, AudioFileFormat.Type.WAVE, fos);
                    fos.flush();
                    
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
               
                //if (RecConn.getLocalCollection().debug)
                System.out.println("--ELAB-- Scritti " + byteWritten + " byte nel file .wave");
                
                collectionAisList.clear();
//                try { //"dimentico" byte gia' scritti
//                    localAis.skip(localAis.available());
//                    remoteAis.skip(remoteAis.available());
//                } catch (IOException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
                
                        
          }//end while
        
        //CHIUSURA FILE E STREAM ASSOCIATO
        
        try {
            fos.flush();
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
                        
/* PROVA DECODIFICA E CONVERSIONE IN PCM
        //creo i due input stream e i due array da scrivere nei file
        
        //if (RecConn.getRemoteCollection().debug)
            System.out.println("--ELAB-- ELABORAZIONE LOCAL COLLECTION");
        
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
                
                //if (RecConn.getRemoteCollection().debug)
                    System.out.println("--ELAB-- Decodificato Sn " + localSn);
                
                try {
                    
                    while (localAis.available() < 320)
                    {
                        Thread.sleep(100);
                      //if (RecConn.getRemoteCollection().debug)
                        System.out.println("--ELAB-- attendo 100ms per leggere la decodifica di Sn " + localSn);
                    }
                    localAis.read(localArray, i*320 , 320);
                    //if (RecConn.getRemoteCollection().debug)
                        System.out.println("--ELAB-- Sn " + localSn + "scritto nel localArray");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
            }
            else
                break;
        }
        
        if (RecConn.getRemoteCollection().debug)
            System.out.println("--ELAB-- ELABORAZIONE REMOTE COLLECTION");
        
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
                
                if (RecConn.getRemoteCollection().debug)
                    System.out.println("--ELAB-- Decodificato Sn " + remoteSn);
                
                try {
                    remoteAis.read(remoteArray, j*320 , 320);
                    
                    if (RecConn.getRemoteCollection().debug)
                        System.out.println("--ELAB-- Sn " + remoteSn + "scritto nel localArray");
                    
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
 
*/
          
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