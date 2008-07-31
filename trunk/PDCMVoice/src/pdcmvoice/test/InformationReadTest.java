/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.test;
import java.io.*;
import javax.sound.sampled.UnsupportedAudioFileException;
import pdcmvoice.settings.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import jlibrtp.Participant;
import jlibrtp.RTPSession;
import pdcmvoice.impl.Information;
import pdcmvoice.impl.VoiceSessionReceiver;
import static pdcmvoice.impl.Constants.*;

/**
 *
 * @author Laura
 */
public class InformationReadTest  {
   
    public static void main(String args[]) throws IOException, ClassNotFoundException, UnsupportedAudioFileException, Exception
{
        //MIO/RECEIVER
        ConnectionSettings connMio = new ConnectionSettings(8765, 7000, 7001,1222);
        AudioSettings auodioMio = new AudioSettings();
        TransmissionSettings tsMio = new TransmissionSettings();        
       
        Socket socket = new Socket("localhost",connMio.getMaster());

        Information i = new Information(auodioMio, connMio, tsMio);
//        File theFile= new File("C:/provaRec.bin");	
//        FileOutputStream fo = new FileOutputStream(theFile);
//        ObjectOutputStream o = new ObjectOutputStream(fo);
         OutputStream output = socket.getOutputStream();
         ObjectOutputStream o = new ObjectOutputStream(output);
//        //scrivo le info
        o.writeObject(i);     
//        //chiudo gli stream
//        o.flush();
//        o.close();
//        fo.flush();
//        fo.close();
        
        //SENDER
        ConnectionSettings connSender;
        AudioSettings audioSender;
        VoiceSessionSettings voiceSessionSettings;
        TransmissionSettings trasmSender;
        Information ii = null;
        int formatCode = 0;
        int speexQuality = 0;
        int masterport = 0;
        int rtcpPortSender = 0;
        int rtpPortSender = 0;
        int recoveryPortSender =0;
        int rtcpPort = 0;
        int rtpPort = 0;
        int recoveryPort =0;
        String addr;
        
        InputStream in= socket.getInputStream();
        ObjectInputStream instream = new ObjectInputStream(in);                   
        
//        File file = new File("C:/prova.bin");
//        if(!file.exists())
//            System.err.println("Il file non esiste");
//        FileInputStream fi = new FileInputStream(file);
//        ObjectInputStream in = new ObjectInputStream(fi);
      
//        try{
           System.out.println("Porte usate dal sender;");
           ii= (Information)instream.readObject();
           instream.close(); 
//           fi.close();         
//        }catch(OptionalDataException ode){
//            ode.printStackTrace();
//    }
//        catch(ClassCastException cce){
//            System.err.println();
//        }
           
        //estraggo i parametri del sender
        
            //audiSettings
            audioSender = ii.getAudioSettings();
            formatCode = audioSender.getFormat();
            speexQuality = audioSender.getSpeexQuality();
            
            //connectionSettings
            connSender = ii.getConnectionSettings();
            masterport = connSender.getMaster();
            rtcpPortSender = connSender.getRTCP();
            rtpPortSender = connSender.getRTP();
            recoveryPortSender = connSender.getRecovery();
            
            //transmission settings
            trasmSender = ii.getTransmissionSettings();
          
   
    }
}
