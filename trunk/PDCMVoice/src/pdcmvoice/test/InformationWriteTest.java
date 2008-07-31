/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.test;


import java.io.*;
import pdcmvoice.settings.*;
import java.net.*;
import pdcmvoice.impl.Information;
import static pdcmvoice.impl.Constants.*;


/**
 *
 * @author Laura
 */
public class InformationWriteTest {
    
public static void main(String args[]) throws IOException, ClassNotFoundException,NullPointerException, Exception{
        
        AudioSettings a = new AudioSettings(2,3);
        ConnectionSettings c = new ConnectionSettings(DEFAULT_MASTER_PORT,9000,9001,1222);
        TransmissionSettings ts = new TransmissionSettings();
        
        ServerSocket server = new ServerSocket(c.getMaster());
        Socket socket = server.accept();
        
        InputStream input = socket.getInputStream();    
        OutputStream out = socket.getOutputStream();
        Information i = new Information(a,c,/*vss,*/ts);
        
       // File theFile= new File("C:/prova.bin");	
        //FileOutputStream fo = new FileOutputStream(theFile);
       // ObjectOutputStream o = new ObjectOutputStream(fo);
        ObjectOutputStream o = new ObjectOutputStream(out);
        //scrivo le info
        o.writeObject(i);     
        //chiudo gli stream
//        o.flush();
//        o.close();
//        fo.flush();
//        fo.close();
        
        Information ii=null;
//         File file = new File("C:/provaRec.bin");
//        if(!file.exists())
//            System.err.println("Il file non esiste");
//        FileInputStream fi = new FileInputStream(file);
//        ObjectInputStream in = new ObjectInputStream(fi);
//        System.out.println(in.available());
//        try{
        System.out.println("porte usate dal receiver");
        ObjectInputStream in = new ObjectInputStream(input);   
        ii= (Information)in.readObject();
//           in.close();
//           fi.close();         
//        }catch(OptionalDataException ode){
//            ode.printStackTrace();
//    }
//        catch(ClassCastException cce){
//            System.err.println();
//        }
        VoiceSessionSettings vss = new VoiceSessionSettings(a, c, ts,
                                  ii.getAudioSettings(), ii.getConnectionSettings(),
                                  "127.0.0.1");
        in.close();
        o.flush();
        o.close();
    }
}
