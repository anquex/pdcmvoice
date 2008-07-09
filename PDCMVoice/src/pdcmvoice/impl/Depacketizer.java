/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.impl;

import com.sun.org.apache.bcel.internal.generic.IFEQ;
import java.net.InetSocketAddress;
import jlibrtp.DataFrame;
import jlibrtp.DebugAppIntf;
import jlibrtp.Participant;
import jlibrtp.RTPAppIntf;
import jlibrtp.RTPSession;

/**
 *
 * @author marco
 */
public class Depacketizer implements RTPAppIntf,DebugAppIntf{
    
    private final boolean DEBUG=false;
    
    private RTPSession rtpSession;
    private Decoder decoder;
    private boolean registered;
    private long lastReceivedSN=0;
    private boolean inited;
    
//    int avg=0;
//    int packets=0;
//    long LastTimeStamp=0;
//    long currentTimeStamp=0;
    
    public Depacketizer(RTPSession s){
        rtpSession=s;
        if(DEBUG) rtpSession.RTPSessionRegister(this, null, this);
        else rtpSession.RTPSessionRegister(this, null, null);
        
    }
    public synchronized void receiveData(DataFrame frame, Participant participant)
    {   
        if (!inited) return;
        
//        if(LastTimeStamp==0) LastTimeStamp=System.currentTimeMillis();
//        currentTimeStamp=System.currentTimeMillis();
//        System.out.println(currentTimeStamp-LastTimeStamp);
//        LastTimeStamp=currentTimeStamp;
        byte[] data = frame.getConcatenatedData();
        long timestamp=frame.rtpTimestamp();
        int SN=frame.sequenceNumbers()[0];
        if (lastReceivedSN>SN) {
//            System.out.println("OutOfOrder");
            decoder.decodeFrame(null,SN,timestamp);
        }
        decoder.decodeFrame(data,SN,timestamp);
        //System.out.println("pacchetto ricevuto");
        
    }
    
    // to prevent null pointer exception
    public void init(){
        inited=true;
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

    public void packetReceived(int type, InetSocketAddress socket, String description) {
        //nothing
    }

    public void packetSent(int type, InetSocketAddress socket, String description) {
        //nothing
    }

    public void importantEvent(int type, String description) {
        //nothing
    }

}
