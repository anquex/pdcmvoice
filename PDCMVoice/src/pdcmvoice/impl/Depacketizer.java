/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.impl;

import jlibrtp.DataFrame;
import jlibrtp.Participant;
import jlibrtp.RTPAppIntf;
import jlibrtp.RTPSession;

import static pdcmvoice.impl.Constants.*;

import java.util.Arrays;

/**
 *
 * @author marco
 */
public class Depacketizer implements RTPAppIntf{
    
    private final boolean DEBUG=false;
    
    private RTPSession rtpSession;
    private Decoder decoder;
    private boolean registered;
    private long lastReceivedSN=0;
    private boolean inited;
    
    //DEBUG
    private byte[] lastNewVoice;
    
//    int avg=0;
//    int packets=0;
//    long LastTimeStamp=0;
//    long currentTimeStamp=0;
    
    public Depacketizer(RTPSession s){
        rtpSession=s;
        rtpSession.RTPSessionRegister(this, null, null);
        
        
    }
    public synchronized void receiveData(DataFrame frame, Participant participant)
    {   
        if (!inited) return;
//        if(LastTimeStamp==0) LastTimeStamp=System.currentTimeMillis();
//        currentTimeStamp=System.currentTimeMillis();
//        System.out.println(currentTimeStamp-LastTimeStamp);
//        LastTimeStamp=currentTimeStamp;
        
        byte[] data = frame.getConcatenatedData();
        boolean isRDT=isRDT(frame.payloadType());
        
//        out("FRAME :"+frame.payloadType());
        long timestamp=frame.rtpTimestamp();
        int SN=frame.sequenceNumbers()[0];
        if (isRDT){
            int prevoiusSN=SN-1;
            //always integer
            int lenght=data.length/2;
            byte[] newVoice= new byte[lenght];
            byte[] previousVoice=new byte[lenght];
            System.arraycopy(data, 0, newVoice, 0, lenght);
            System.arraycopy(data, lenght, previousVoice, 0, lenght);
            
            decoder.decodeFrame(newVoice,SN,timestamp);
           
            
            //DEBUG 
            
           
            
            //Print new voice frame
            
//            System.out.println("New Voice :"+frame.sequenceNumbers()[0]);
//            String out="";
//            for(int i=0;i<newVoice.length;i++)
//                out+=" "+newVoice[i];
//            System.out.println(out);
//            
//            System.out.println("Previous Voice :"+frame.sequenceNumbers()[0]);
//            out="";
//            for(int i=0;i<previousVoice.length;i++)
//                out+=" "+previousVoice[i];
//            System.out.println(out);
            
            //print previous voice frame
            
//            if (lastNewVoice!=null){
//                if(Arrays.equals(lastNewVoice, previousVoice))
//                     System.out.println("coincidono");
//                else System.out.println("non coincidono");
//               
//            }
//            lastNewVoice=newVoice;
            
            
        }
        else{
           decoder.decodeFrame(data,SN,timestamp);
        }
        
        
        if (lastReceivedSN>SN) {
//            System.out.println("OutOfOrder");
            decoder.decodeFrame(null,SN,timestamp);
        }
        //System.out.println("pacchetto ricevuto");
        
    }
    
    // to prevent null pointer exception
    public void init(){
        inited=true;
    }
    
    private boolean isRDT(int payloadType){
        if (payloadType==PAYLOAD_SPEEX_RDT ||
            payloadType==PAYLOAD_iLBC_RDT)
            return true;
        // unknown payload or not RDT
        else return false;
                
    }
    
    
    public boolean registerDecoder(Decoder d){
      //  if (d==null) throw new NullPointerException("Null is not a valid Decoder");
        if(registered) {
                System.out.println("Depacketizer: Can\'t register another decoder!");
                return false;
        } else {
                registered = true;
                //System.out.println("Decoder Registered");
                this.decoder=d;
                return true;
        }   
    }

    public void userEvent(int type, Participant[] participant) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public int frameSize(int payloadType) {
        // 1 packet -> at least 1 frame
        return 1;
    }

}
