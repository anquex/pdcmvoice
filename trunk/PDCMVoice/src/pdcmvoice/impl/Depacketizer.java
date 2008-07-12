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
import java.util.Collections;
import java.util.Comparator;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TreeMap;
import java.util.TreeSet;

import java.util.TimerTask;

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
    
    PlayoutBuffer playoutBuffer;
    
    //DEBUG
    private byte[] lastNewVoice;
    
//    int avg=0;
//    int packets=0;
//    long LastTimeStamp=0;
//    long currentTimeStamp=0;
    
    public Depacketizer(RTPSession s){
        rtpSession=s;
        rtpSession.RTPSessionRegister(this, null, null);
        // disable rtp buffering, recive all packets!
        rtpSession.packetBufferBehavior(0);
        playoutBuffer=new PlayoutBuffer();
        
        
    }
    public synchronized void receiveData(DataFrame frame, Participant participant)
    {   
        if (!inited) return;
        
        playoutBuffer.add(frame);
        
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

    
    
    class PlayoutBuffer{
        
        private int minBuffered=0;
        private int maxBuffered=0;
        private SortedSet listBuffer;
        private int lowerReceivedSN;
        private int higherReceivedSN;
        private boolean playing;
        private Timer timer;
        Deliver periodicPopper;
        private boolean isFirst=true;
        private short expectedSN;



        public PlayoutBuffer() {
            this(3);
        }
        
        public PlayoutBuffer(int size) {
            this(size,size+2);
  
        }
        public PlayoutBuffer(int minsize, int maxsize) {
            if (minsize<1 || maxsize<minsize) throw new IllegalArgumentException();
            //listBuffer=Collections.synchronizedSortedSet(new TreeSet());
            // synchronized using PlayoutBuffer methods
            listBuffer=new TreeSet(new DataFrameComparator());
            this.minBuffered=minsize;
            this.maxBuffered=maxsize;
            
            
            
        }
        
        public synchronized void add(DataFrame f){
            out ("frame ricevuto");
            if(isFirst){
                isFirst=false;
                lowerReceivedSN=f.sequenceNumbers()[0];
                higherReceivedSN=lowerReceivedSN;
            }
            lowerReceivedSN=Math.min(f.sequenceNumbers()[0],lowerReceivedSN);
            listBuffer.add(f);
            out ("frame nel buffer : "+size());
            if (size()>maxBuffered){
                //over run
                //drop older packet
                listBuffer.remove(listBuffer.first());
                expectedSN++;
                
            }
            else if (size()==minBuffered && !playing){
                // 
                expectedSN=(short)((DataFrame)listBuffer.first()).sequenceNumbers()[0];
                //start decoding frames
                // Timer as daemon
                timer= new Timer("Playout Buffer Delivery Timer", false);
                // play a frame every 20ms
                periodicPopper = new Deliver();
                timer.scheduleAtFixedRate(periodicPopper, 0, 20);
                playing=true;
                // Start Timer
            }
            
        }
        
        public synchronized int size(){
            return listBuffer.size();
        }
        
        public synchronized boolean isEmpty(){
            return listBuffer.isEmpty();
        }
        
        public synchronized DataFrame remove(){
        // 1) Buffer Empty     
            if (isEmpty()){ 
                // return in buffering state
                playing=false;
                // wait for buffer to be full before restart playing
                timer.cancel();
                // notify decoder of the event
                return null;
            }
        //  2) This packet has been lost but I still have something to
        //     play for next future
            DataFrame extracted=(DataFrame)listBuffer.first();
            if (expectedSN<extracted.sequenceNumbers()[0]){
                // a packet is missing (out of time or lost)
                expectedSN++;
                return null;
                
            }
        // 3) Play the packet that should be played and remove from
        // the buffer
            else{
                listBuffer.remove(extracted);
                expectedSN++;
                return extracted;
            }
            
        }
        
        class DataFrameComparator implements Comparator{

            public int compare(Object o1, Object o2) {
                DataFrame f1= (DataFrame) o1;
                DataFrame f2= (DataFrame) o2;
                return f1.sequenceNumbers()[0]-f2.sequenceNumbers()[0];
            }
        }
        
        class Deliver extends TimerTask {
            int n;
            
            public void run() {
                n++;
                out("mando frame " +n);
                DataFrame frame=remove();
                if (frame==null){
                    decoder.decodeFrame(null, 0, 0);
                    
                }
                else{
                  byte[] data = frame.getConcatenatedData();
                  boolean isRDT=isRDT(frame.payloadType());
                  //out("FRAME :"+frame.payloadType());
                  long timestamp=frame.rtpTimestamp();
                  int SN=frame.sequenceNumbers()[0];
                if (isRDT){
                    //always integer
                    int lenght=data.length/2;
                    byte[] newVoice= new byte[lenght];
                    byte[] previousVoice=new byte[lenght];
                    System.arraycopy(data, 0, newVoice, 0, lenght);
                    System.arraycopy(data, lenght, previousVoice, 0, lenght);
                    decoder.decodeFrame(newVoice,SN,timestamp);


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
            }
        }//Deliver
    }//Playout Buffer
 
}
