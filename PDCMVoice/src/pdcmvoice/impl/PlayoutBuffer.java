/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import static pdcmvoice.impl.Constants.*;

/**
 *
 * @author marco
 */
public class PlayoutBuffer{

    private boolean isFirst;
    private long startPacketTimestamp;
    private boolean isBuffering;
    private Decoder decoder;
    private int minBufferedMillis=60;

    // must be at least 20 ms!
    private int maxBufferedMillis=120;
    private SortedSet<VoiceFrame> listBuffer;
    
    private Timer timer;
    private Deliver decoderDeliver;
    
    private final boolean DEBUG=true;

    //CONSTANTS

    public final int TIME_PER_FRAME = 20; // in ms



    public PlayoutBuffer(Decoder d) {
        decoder=d;
        listBuffer=Collections.synchronizedSortedSet(new TreeSet<VoiceFrame>(new VoiceFrameComparator()));
        isFirst=true;
        isBuffering=true;
        decoderDeliver= new Deliver();
    }


    public synchronized void add(long timestamp, byte[] frame){
        /* We consider the timestamp of the first packet received as first*/
        
//        if(timestamp<=decoderDeliver.getNextTimestampToPlay())
//            return;

        VoiceFrame v=new VoiceFrame(timestamp, frame);
        listBuffer.add(v);

        if (DEBUG) out("BUFFER: Frame Added : new size... "+size());

        if (isFirst){
            startPacketTimestamp=timestamp;
            isFirst=false;
            if (DEBUG) out("BUFFER: First Frame recived: First timestamp "
                           +startPacketTimestamp);
        }

        // bound max delay

        // bursts arrivals produces more packets are dropped at once

        // this code is never exectuted if I have only 1 packet or I'm buffering
        if(getHigherTimestamp()-getLowerTimestamp()+20>maxBufferedMillis
              && !isBuffering)
        {
            if (DEBUG)
                    out("PLAYOUT BUFFER : Maximum Delay Reached: "+
                    (getHigherTimestamp()-getLowerTimestamp()+20));

            // drop the packet

            remove();

            if(DEBUG)  out("PLAYOUT BUFFER : Packet Dropped due to High Latency");

            // start playing from the older packet

            decoderDeliver.startPlaying(getLowerTimestamp());

        }



        if (isBuffering){
            Iterator<VoiceFrame> iter=listBuffer.iterator();

            // at least 1 element is present since I just made an add

            long currentTimestamp=getLowerTimestamp();

            // count how many millis I have in the buffer

            // I want to have consecutive packets before starting
            // playing back

            int bufferedMillis=0; // millis in buffer
            while(iter.hasNext()){

                VoiceFrame next=iter.next();

                long nextstamp=next.getTimestamp();
                
                if (nextstamp==currentTimestamp){
                    
                    bufferedMillis+=20;

                    // next packet referst to istant t+20
                    currentTimestamp=nextstamp+20;

 //                   if (DEBUG) out("BUFFER : Buffered "+bufferedMillis+" millis" );

                    if(bufferedMillis>=minBufferedMillis){
                        // I have enought consecutive voice frame 
                        // in the buffer to start playback
                        isBuffering=false;

                        if(DEBUG){
                            out("-------   Buffering Complete   --------");
                        }

                        // send a frame to decoder each 20 ms
                        if(timer==null){
                            timer=new Timer("Playout Buffer Timer");
                           //timer.schedule(decoderDeliver,0,20);
                            timer.scheduleAtFixedRate(decoderDeliver,0,20);
                        }

                        // start playing from older frame

                        decoderDeliver.startPlaying(getLowerTimestamp());
                        break;
                    }
                    else{
//                       if(DEBUG)
//                       out("Still Buffering...");
                    }
                }
            }
        }
        // Have to manage brust lenght
        
        //decoder.decodeFrame(frame, 0, timestamp);
        
    }
    
    public synchronized  long getLowerTimestamp(){
        return listBuffer.first().getTimestamp();
    }
    
    public synchronized  long getHigherTimestamp(){
        return listBuffer.last().getTimestamp();
    }
    
    private synchronized  VoiceFrame remove(){
        if (isEmpty()){
            // nothing to play or remove
            if (DEBUG) out("BUFFER : Buffer Empty");

            isBuffering=true;
            if (DEBUG) out("BUFFER : Starting Buffering again...");
            return null;
        }

        // there is still something in the buffer

        VoiceFrame older=listBuffer.first();
        listBuffer.remove(older);
        return older;
    }

    public synchronized int setMinBufferedMillis(int n){
        if (minBufferedMillis>=0)
            minBufferedMillis=n;
        return minBufferedMillis;
    }
    
    public synchronized  int size(){
        return listBuffer.size();
    }

    public synchronized  boolean isEmpty(){
        return listBuffer.isEmpty();
    }

    class Deliver extends TimerTask {
        
        private int samplesPlayed;
        private long nextTimestampToPlay=-1;
        private boolean first=true;
        private boolean isPlaying;
        
        public void startPlaying(long firstTimestamp){
            if (DEBUG)
                out ("DELIVER: Start Playing from frame "+firstTimestamp);
            nextTimestampToPlay=firstTimestamp;
            isPlaying=true;
        }
        public  void stopPlaying(){
            if (DEBUG) out ("DELIVER: Stop  Playing");
            isPlaying=false;
            nextTimestampToPlay=-1;
        }

        public  long getNextTimestampToPlay(){
            return nextTimestampToPlay;
        }
        
        public  boolean isPlaying(){
            return isPlaying;
        }  
               

        public  void run() {
            if (isPlaying()){
                synchronized(PlayoutBuffer.this){
                    if(isEmpty()){
                        // nothing to play
                        isBuffering=true; //Playout buffer
                       // stop playing since I don't have nothing to play
                        stopPlaying();
                        return;
                    }
    //                out("lower"+getLowerTimestamp());
    //                out("next"+nextTimestampToPlay);
                    // if a packet is missing it meeans that getLowerTimestamp()
                    // is higher than nextTimestampToPlay
                    // if it is lower it means that I'm introducing unnecessary
                    // delay
    //                if (getLowerTimestamp()<nextTimestampToPlay)
    //                    nextTimestampToPlay=getLowerTimestamp();
                    // I want to have bounded delay, max acceptable delay is
                    // maxBufferedMillis
                    // max delay should be managed by add method
    //                if (getHigherTimestamp()>maxBufferedMillis+nextTimestampToPlay)
    //                    nextTimestampToPlay=getLowerTimestamp();


                    if (getLowerTimestamp()==nextTimestampToPlay){
                        samplesPlayed++;
                        // in the buffer there is what i want to play
                        //send to the decoder
                        VoiceFrame vf=remove();
                        decoder.decodeFrame(vf.getContent());
                        if (DEBUG) out("DELIVER: Playing Frame : "+nextTimestampToPlay);
                    }
                    else{
                        //notify the decoder of the problem (PL?)
                        if (DEBUG) out("DELIVER: Packet loss , " +
                                       "expeted Frame :"+nextTimestampToPlay);
                        decoder.decodeFrame(null);
                    }
                    nextTimestampToPlay+=20;
                }
    //            else{
    //                //synchronize while not playing to first playable sample
    //                if (!isEmpty()){
    //                    nextTimestampToPlay=getLowerTimestamp();
    //                   // isPlaying=true;
    //                }
    //            }
            }
        }
    }//Deliver
    
   
    class VoiceFrameComparator implements Comparator<VoiceFrame>{

        public int compare(PlayoutBuffer.VoiceFrame o1, PlayoutBuffer.VoiceFrame o2) {
             return (int)(o1.getTimestamp()-o2.getTimestamp());
        }

    }
    class VoiceFrame{
        private long timestamp;
        private byte[] frame;
        
        VoiceFrame(long timestamp, byte[] frame){
            this.timestamp=timestamp;
            this.frame=frame;  
        }
        long getTimestamp(){
            return timestamp;
        }
        byte[] getContent(){
            return frame;
        }
    }
}//Playout Buffer
