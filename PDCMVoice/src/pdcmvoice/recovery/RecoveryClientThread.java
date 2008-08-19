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
public class RecoveryClientThread extends Thread
{
    private RecoveryConnection RecConn;
    private String lastQuery;
    private byte[] lastQueryByte;
    private boolean lastQueryDone;
    public boolean endOfStream;
    //private RecoveryServerThread server; //NO! il serverThread si ferma da solo quando legge lastQuery == "END OF QUERY"!!! - serve per interrompere l'esecuzione del ServerThread
    private boolean stopQuery;
    private VoiceSession voiceSession;
    
    public RecoveryClientThread(RecoveryConnection RecConn, VoiceSession voiceSession)
	{
        this.RecConn = RecConn;
        lastQuery = null;
        lastQueryDone = false;
        endOfStream = false;
        stopQuery = false;
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
                    System.out.println("--CLIENT-- Acquisizione dimensione pacchetto codificato");
                
                try {
                    Thread.sleep(500); //attesa durante l'acquisizione della dimensione
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
            this.RecConn.getRemoteCollection().setPktSize(voiceSession.lastEncodedFrameSize());
            if (RecConn.debug)
                System.out.println("--CLIENT-- Dimensione pacchetto codificato: " + voiceSession.lastEncodedFrameSize());
	    }
	    
	    
	    /*
	     * RICORDA LA RIGA DI DEBUG (simulazione perdita di pacchetti)
	     * 
	     * in Depacketizer.java
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
	    //byte[] temp = new byte[pktSize];  NON QUI'!!!!!
        long zero = 0;
        boolean rtpDown;
        
        try {
            Thread.sleep(7000); //attesa prima di partire
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


     //      ----------------------------
     //       QUERY COSTITUITE DA BYTE
     //      ----------------------------       

          //##LIMITAZIONE AUTOMATICA DELLA VITA DEL THREAD DOPO UN TOT DI QUERY INVIATE
            if (pdcmvoice.impl.Constants.RECOVERY_CLIENT_THREAD_LIMITED_LIFE_DEBUG && writingTest++ >= 7)
            {   
                System.out.println("--CLIENT-- [**DEBUG**] INTERRUZIONE DELLE RICHIESTE: RECOVERY_CLIENT_THREAD_LIMITED_LIFE_DEBUG = true");
                System.out.println("--CLIENT-- [**DEBUG**]                               writingTest HA RAGGGIUNTO IL VALORE PREIMPOSTATO");
                stopQuery = true;
                lastQueryByte = RecConn.getRemoteCollection().findAllHolesByte();
                System.out.println("--CLIENT-- [**DEBUG**] lastSnReceived: " + RecConn.getRemoteCollection().lastSnReceived);
            }
            else
          //##FORMULAZIONE STANDARD DELLE QUERY
            {      //if (!rtpDown)
                  if (!endOfStream) //da impostare tramite classi condivise come VoiceSessionSettings
                      lastQueryByte = RecConn.getRemoteCollection().findHolesByte(0, false);
                  else
                  {    
                      lastQueryByte = RecConn.getRemoteCollection().findAllHolesByte();
                      stopQuery = true;
                  }
            }    
                  
            
             // ATTENZIONE!
             // Il primo burst lo perdo sempre se perdo il primo pacchetto
             
            //##INVIO DELLA QUERY
              if (lastQueryByte != null)
              {
                //##DEBUG
                  if (RecConn.getRemoteCollection().debug)
                  {
                      System.out.print("INVIO QUERY:");
                      for (int k=0; k<=lastQueryByte.length-1; k++) //k <= lastQueryByte[0] + lastQueryByte[1]
                          System.out.print(" " + lastQueryByte[k]);
                      System.out.println("");
                  }
                  
                  try {
                      if (RecConn.getServerSocket().isConnected())
                      {
                          dos.write(lastQueryByte, 0, lastQueryByte.length);
                          dos.flush();
                      }
                      else if (RecConn.getRemoteCollection().debug)
                      {
                          System.out.println("--CLIENT-- CONNESSIONE RECOVERY ASSENTE");
                      }
                      
                  } catch (IOException e) {
                      // TODO Auto-generated catch block
                      e.printStackTrace();
                  }
                  
                 
                //##ELABORAZIONE DELLA QUERY
                  
                //##LUNGHEZZA
                  int length = RecoveryCollection.mergeBytes(lastQueryByte[0], lastQueryByte[1]) ;         //lunghezza della query vera e propria (esclusi i 3 byte che indicano la lunghezza stessa)
    //                  if (RecConn.getRemoteCollection().debug)
    //                      System.out.println("--CLIENT-- length della query effettiva= " + length);
                  
                  int sn  = 0;
                  byte separatore = 0;
                  int start, end;
                  
                  int firstSnOfTheQuery = 0;
                  
                //##CONTENUTO DELLA QUERY
                  for (int j = 3; j<=(length+3) -1; j++) //i primi 3 elementi della query rappresentano la sua lunghezza e sono gi� stati elaborati
                  {
    //                      if (RecConn.getRemoteCollection().debug)
    //                          System.out.println("--CLIENT-- entro nel ciclo for con length = " + length);
                      
                      sn = RecoveryCollection.mergeBytes(lastQueryByte[j],  lastQueryByte[++j]);
                      separatore = lastQueryByte[++j];
                      
                      if (j == 5)
                          firstSnOfTheQuery = sn;
                      else
                          sn += firstSnOfTheQuery; //considero vecchio sn come incremento
                      
                      if (RecConn.getRemoteCollection().debug)
                      {
                      System.out.println("--CLIENT-- firstSnOfTheQuery: " + firstSnOfTheQuery);
                      System.out.println("--CLIENT-- sn: " + sn);
                      }
                      
                    //##INDIVIDUAZIONE DI START ED END
                      start = sn;
                      if (separatore == 0)
                          end = start;
                      else if (separatore == 1)
                      {
                          end = firstSnOfTheQuery + RecoveryCollection.mergeBytes(lastQueryByte[++j], lastQueryByte[++j]);
                          j++; //salto il separatore successivo
                          
                          if (RecConn.getRemoteCollection().debug)
                              System.out.println("--CLIENT-- end: " + end);
                      }
                      else
                          throw new IllegalArgumentException();
                      
                    //##LETTURA DEI PACCHETTI TRA GLI INDICI START ED END COMPRESI DALLO STREAM DI INPUT: LI HA INVIATI COME RISPOSTA ALLA QUERY IL SERVER DELL'INTERLOCUTORE
                      for (int i = start; i <= end; i++)
                      {
                          byte[] temp = new byte[pktSize];
                          try {
    //                              if (RecConn.getRemoteCollection().debug)
    //                                  System.out.println("--CLIENT-- tentativo lettura pacchetto " + i);
                           while (dis.available() <= 0)
                           {
                               try {
                                   Thread.sleep(20); //attendi che il dis diventi available
                               } catch (InterruptedException e1) {
                                   // TODO Auto-generated catch block
                                   e1.printStackTrace();
                               }
                               
                               if (RecConn.getRemoteCollection().debug)
                                  System.out.println("--CLIENT-- DATA INPUT STREAM ASSENTE");
                               
                           }//end while (dis.available() > 0)
                           dis.read(temp, 0, pktSize);
                           
    //                          if (RecConn.getRemoteCollection().debug)
    //                              System.out.println("--CLIENT-- LETTO pacchetto " + i);
                          
                          } catch (IOException e) {
                              // TODO Auto-generated catch block
                              e.printStackTrace();
                          }
                          
                        //##RECUPERO EFFETTIVO DEL PACCHETTO: AGGIUNTA ALLA COLLEZIONE
                          RecConn.getRemoteCollection().recover(i, temp);
                          //RecConn.getRemoteCollection().add(i, temp, zero);
                        
                        //##DEBUG
                          if (RecConn.getRemoteCollection().debug)
                              System.out.println("pkt recuperato dal ClientThread: " + i);
                          
                        if (RecConn.debug)
                          {
                              System.out.print("--CLIENT-- Ricevuto Sn " + i + ": ");
                              
                              for (int p = 0; p <= temp.length -1; p++)
                              {
                                  System.out.print(temp[p] + " ");
                              }
                              
                              System.out.println("lunghezza=  " + temp.length);
                              
                              System.out.print("--CLIENT-- inserito Sn " + i + ": ");
                              
                              for (int p = 0; p <= RecConn.getRemoteCollection().read(i).length -1; p++)
                              {
                                  System.out.print(RecConn.getRemoteCollection().read(i)[p] + " ");
                              }
                              
                              System.out.println("lunghezza=  " + temp.length);
                              
                           } //end if debug
                         
                      }
                      
                  }
                  
                  lastQueryByte = null;
              }
              else if (RecConn.getRemoteCollection().debug)
                  System.out.println("ClientThread: nessun pkt perso dall'ultima ricerca");
        
        }//end while
        
        /*
         * TODO prima di finire le richieste DEVO RECUPERARE CON UN'ULTIMA RICHIESTA GLI EVENTUALI PACCHETTI INIZIALI NON ARRIVATI.
         * li richiedo al serverThread dell'interlocutore che cercherà fino all'inizio della propria localCollection e li invierà in sequenza in modo da permettere al mio client di concatenare questo insieme di pacchetto con quello da lui posseduto (la mia remote collection)
         */
        
        
      //##COMUNICAZIONE DELLA FINE DELLE RICHIESTE
        
        //informa il server dell'altro interlocutore che sono stati ricevuti tutti i pacchetti
        try {
            byte a, b, c;
            a = 0; b = 0; c = 2;
            if (RecConn.getServerSocket().isConnected())
            {
                dos.write(new byte[]{a, b, c}, 0, 3);
                dos.flush();
            }
            else if (RecConn.getRemoteCollection().debug)
            {
                System.out.println("--CLIENT-- CONNESSIONE RECOVERY ASSENTE, IMPOSSIBILE COMUNICARE \"END OF STREAM\" AL SERVER REMOTO");
            }
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
         
      //##ELABORAZIONE DELLE COLLEZIONI E SCRITTURA DEL FILE DI SALVATAGGIO DELLA CONVERSAZIONE
         
        
        if (RecConn.getRemoteCollection().debug)
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
        
        AudioFormat targetAudioFormat = new AudioFormat (AudioFormat.Encoding.PCM_SIGNED, new Float(16000.0), localAis.getFormat().getSampleSizeInBits(), localAis.getFormat().getChannels(), localAis.getFormat().getFrameSize(), localAis.getFormat().getFrameRate(), localAis.getFormat().isBigEndian());
        
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
         
        
        STRATEGIA 3
        a partire dagli stream OPPURE dai file .wav "separati" (utile per gestione delle lunghezze)
        preparazione degli stream per MixingAudioInputStream..come?? come "aggiornamento" della strategia 2??
        mixaggio con audioInputStream = new MixingAudioInputStream(audioFormat, audioInputStreamList);
        e scrittura su file
        */
        
       
        /*
         * STRATEGIA 2
         */
        
      if (RecConn.getLocalCollection().debug)
        System.out.println("--ELAB-- ELABORAZIONE LOCAL COLLECTION");
    
        int i = 0;
        for (; ; i++)
        {
            localSn = local.getFirstSnReceived() + i;
           
            if (local.read(localSn) != null)
            {
                //localDecoder.decodeFrame(local.read(localSn), localSn, 0);
                localDecoder.decodeFrame(local.read(localSn));
                
                if (RecConn.getLocalCollection().debug)
                {
                    System.out.print("--ELAB-- Decodificato Sn " + localSn + ": ");
                    
                    for (int l = 0; l <= local.read(localSn).length -1; l++)
                    {
                        System.out.print(local.read(localSn)[l] + " ");
                    }
                    
                    System.out.println("");
                }
                
            }
            else
            {
              if (RecConn.getLocalCollection().debug)
                System.out.println("--ELAB-- Trovato pacchetto NULL");
                break;
            }
        }
        
      if (RecConn.getLocalCollection().debug)
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
        
//        try {
////            while (localAis.available() < 10)
////            {
////                Thread.sleep(50);
////              //if (RecConn.getLocalCollection().debug)
////                System.out.println("--ELAB-- attendo 50ms per l'AudioInputStream");
////            }
//            int byteRead = localAis.read(localArray, 0, 320);
//             
//          //if (RecConn.getLocalCollection().debug)
//            {
//            System.out.print("--ELAB-- localArray: ");
//            for (int f = 0; f <= byteRead - 1; f++)
//                System.out.print(localArray[f] + ",");
//            System.out.println("");
//            }
//                
//            } catch (Exception e) {
//                // TODO: handle exception
//                e.printStackTrace();
//            }    
        
        
        
        if (RecConn.getLocalCollection().debug)
        {
            AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes(localAis);  
            for (int f = 0; f <= types.length - 1; f++)
                System.out.print("--ELAB-- localAis types: " + types[f] + ",");
            System.out.println("");
        }
        
        //CODICE PER L'EVENTUALE SCRITTURA DI localAis SU FILE .WAV
        

//      AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes(toBeWritten);
//      //if (RecConn.getLocalCollection().debug)
//            for (int f = 0; f <= types.length - 1; f++)
//            System.out.print("--ELAB-- types: " + types[f] + ",");
       
        
        //Aggiornamento lunghezza IN FRAME dello stream localAis (necessaria per la scrittura del file .wav)
        try {
            localAis = new AudioInputStream(localAis, localAis.getFormat(), localAis.available()/localAis.getFormat().getFrameSize());
            //localAis = new AudioInputStream(localAis, targetAudioFormat, localAis.available()/localAis.getFormat().getFrameSize());
        } catch (IOException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        
        //visualizzazione info sul NUOVO localAis
      if (RecConn.getLocalCollection().debug)
        {
        System.out.println("--ELAB-- AGG. formato localAis: " + localAis.getFormat().toString());
        try {
            System.out.println("--ELAB-- AGG. byte presenti in localAis: " + localAis.available());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("--ELAB-- AGG. lunghezza localAis in frame: " + localAis.getFrameLength());
        System.out.println("--ELAB-- AGG. frame size localAis in byte: " + localAis.getFormat().getFrameSize());
        }
      
//SCRITTURA
        
//        if (RecConn.getLocalCollection().debug)
//        {
//            File file= new File("F:\\local.wav");
//          //if (RecConn.getLocalCollection().debug)
//            System.out.println("--ELAB-- File aperto");
//            
//            if (!file.canWrite())
//              //if (RecConn.getLocalCollection().debug)
//                System.out.println("--ELAB-- ATTENZIONE! Non � possibile scrivere nel file.");
//            
//            FileOutputStream fos = null;
//            try {
//                fos = new FileOutputStream(file);
//            } catch (FileNotFoundException e1) {
//                // TODO Auto-generated catch block
//                e1.printStackTrace();
//            }
//            
//            int byteWritten = 0;
//            try {
//              //if (RecConn.getLocalCollection().debug)
//                System.out.println("--ELAB-- Inizio scrittura del file");
//                
//                byteWritten = AudioSystem.write(localAis, AudioFileFormat.Type.WAVE, fos);
//                
//                fos.flush();
//                fos.close();
//                } catch (IOException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//           
//            //if (RecConn.getLocalCollection().debug)
//            System.out.println("--ELAB-- Scritti " + byteWritten + " byte nel file .wave");
//        }
        
       
        
      
        
      if (RecConn.getRemoteCollection().debug)
        System.out.println("--ELAB-- ELABORAZIONE REMOTE COLLECTION");
    
        i = 0;
        for (; ; i++)
        {
            remoteSn = remote.getFirstSnReceived() + i;
           
            if (remote.read(remoteSn) != null)
            {
                //remoteDecoder.decodeFrame(local.read(remoteSn), remoteSn, 0);
                remoteDecoder.decodeFrame(remote.read(remoteSn));
                
                if (RecConn.getRemoteCollection().debug)
                {
                    System.out.print("--ELAB-- Decodificato Sn " + remoteSn + ": ");
                    
                    for (int l = 0; l <= remote.read(remoteSn).length -1; l++)
                    {
                        System.out.print(remote.read(remoteSn)[l] + " ");
                    }
                    
                    System.out.println("");
                }
                
            }
            else
            {
              if (RecConn.getRemoteCollection().debug)
                System.out.println("--ELAB-- Trovato pacchetto NULL");
                break;
            }
        }
        
      if (RecConn.getRemoteCollection().debug)
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
        
        
        if (RecConn.getRemoteCollection().debug)
        {
            AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes(remoteAis);  
            for (int f = 0; f <= types.length - 1; f++)
                System.out.print("--ELAB-- remoteAis types: " + types[f] + ",");
            System.out.println("");
        }
        
//SCRITTURA DI remoteAis SU FILE .WAV
        

//      AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes(toBeWritten);
//      //if (RecConn.getLocalCollection().debug)
//            for (int f = 0; f <= types.length - 1; f++)
//            System.out.print("--ELAB-- types: " + types[f] + ",");
       
        
        //Aggiornamento lunghezza IN FRAME dello stream remoteAis (necessaria per la scrittura del file .wav)
        try {
            remoteAis = new AudioInputStream(remoteAis, remoteAis.getFormat(), remoteAis.available()/remoteAis.getFormat().getFrameSize());
            //remoteAis = new AudioInputStream(remoteAis, targetAudioFormat, remoteAis.available()/remoteAis.getFormat().getFrameSize());
        } catch (IOException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        
        //visualizzazione info sul NUOVO remoteAis
      if (RecConn.getRemoteCollection().debug)
        {
        System.out.println("--ELAB-- AGG. formato remoteAis: " + remoteAis.getFormat().toString());
        try {
            System.out.println("--ELAB-- AGG. byte presenti in remoteAis: " + remoteAis.available());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("--ELAB-- AGG. lunghezza remoteAis in frame: " + remoteAis.getFrameLength());
        System.out.println("--ELAB-- AGG. frame size remoteAis in byte: " + remoteAis.getFormat().getFrameSize());
        }
      
//SCRITTURA
//      if (RecConn.getRemoteCollection().debug)
//      {
//            File file= new File("F:\\remote.wav");
//          //if (RecConn.getLocalCollection().debug)
//            System.out.println("--ELAB-- File aperto");
//            
//            if (!file.canWrite())
//              //if (RecConn.getLocalCollection().debug)
//                System.out.println("--ELAB-- ATTENZIONE! Non è possibile scrivere nel file.");
//            
//            FileOutputStream fos = null;
//            try {
//                fos = new FileOutputStream(file);
//            } catch (FileNotFoundException e1) {
//                // TODO Auto-generated catch block
//                e1.printStackTrace();
//            }
//            
//            int byteWritten = 0;
//            try {
//              //if (RecConn.getLocalCollection().debug)
//                System.out.println("--ELAB-- Inizio scrittura del file");
//                
//                byteWritten = AudioSystem.write(remoteAis, AudioFileFormat.Type.WAVE, fos);
//                
//                fos.flush();
//                fos.close();
//                } catch (IOException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//           
//            //if (RecConn.getLocalCollection().debug)
//            System.out.println("--ELAB-- Scritti " + byteWritten + " byte nel file .wave");
//        
//      }
        
    //##MIXAGGIO DEI DUE FLUSSI AUDIO
        List collectionAisList = new ArrayList();
        //ATTENZIONE! prova registrazione del solo remoteAis recuperato
        collectionAisList.add(localAis);
        collectionAisList.add(remoteAis);
        
        AudioInputStream mixedAis = new MixingAudioInputStream(localAis.getFormat(), collectionAisList);
        
      if (RecConn.getRemoteCollection().debug)
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
        
        
        if (RecConn.getRemoteCollection().debug)
        {
            AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes(mixedAis);  
            for (int f = 0; f <= types.length - 1; f++)
                System.out.print("--ELAB-- mixedAis types: " + types[f] + ",");
            System.out.println("");
        }
        
        
      //Aggiornamento lunghezza IN FRAME dello stream mixedAis (necessaria per la scrittura del file .wav)
        try {
            remoteAis = new AudioInputStream(mixedAis, mixedAis.getFormat(), mixedAis.available()/mixedAis.getFormat().getFrameSize());
        } catch (IOException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        
        //visualizzazione info sul NUOVO mixedAis
      if (RecConn.getRemoteCollection().debug)
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
          
            String percorso = "F:\\mixed.wav";
            File file= new File(percorso);
            if (RecConn.getLocalCollection().debug)
              System.out.println("--ELAB-- File aperto");
            
            if (!file.canWrite())
            {
                System.out.println("-------------------------------------------------");
                //if (RecConn.getLocalCollection().debug)
                System.out.println("ATTENZIONE! Non e' possibile scrivere nel file:");
                System.out.println(percorso);
                System.out.println("-------------------------------------------------");
                
            }
            
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
            } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                System.out.println("-----------------------------------------------------");
                //if (RecConn.getLocalCollection().debug)
                System.out.println("ATTENZIONE! Non e' stato possibile trovare il file:");
                System.out.println(percorso);
                System.out.println("-----------------------------------------------------");
                
                if (RecConn.getLocalCollection().debug)
                        e1.printStackTrace();
            }
            
            int byteWritten = 0;
            try {
              if (RecConn.getLocalCollection().debug)
                System.out.println("--ELAB-- Inizio scrittura del file");
                
                byteWritten = AudioSystem.write(mixedAis, AudioFileFormat.Type.WAVE, fos);
                
                fos.flush();
                fos.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
           
            //if (RecConn.getLocalCollection().debug)
                System.out.println("");
                System.out.println("");
                System.out.println("---------------------------------------------------------");
                System.out.println("--ELAB-- Scritti " + byteWritten + " byte nel file .wave");
                System.out.println("---------------------------------------------------------");
                System.out.println("");
                System.out.println("");
               
            
            
            voiceSession.rcHasFinished = true; //informa VoiceSession che si e' terminato di svolgere le attivit� di recovery
        
     }
	
	public RecoveryConnection getRecConn()
    {
        return this.RecConn;
    }
	
	private static byte[] arrayResize(byte[] b, int newSize)
    {
        byte[] newArray = new byte[newSize];
        System.arraycopy (b, 0, newArray, 0, b.length);
        return newArray;
    }
	
	
	
}
