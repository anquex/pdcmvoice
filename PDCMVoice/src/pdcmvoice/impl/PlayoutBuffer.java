/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.impl;

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
    private boolean isPlaying;
    private Decoder decoder;
    private int minBufferedMillis=100;
    private int maxBufferedMillis=100;
    private SortedSet<VoiceFrame> listBuffer;
    
    private Timer timer;
    private Deliver periodicPopper;
    
    private final boolean DEBUG=true;



    public PlayoutBuffer(Decoder d) {
        decoder=d;
        listBuffer=new TreeSet<VoiceFrame>(new VoiceFrameComparator());
        isFirst=true;
        isBuffering=true;
        periodicPopper= new Deliver();
    }


    public synchronized void add(long timestamp, byte[] frame){
        /* We consider the timestamp of the first packet received as first*/
        VoiceFrame v=new VoiceFrame(timestamp, frame);
        listBuffer.add(v);
        out("SIZE :"+size());
        if (isFirst){
            startPacketTimestamp=timestamp;
            isFirst=false;
        }
        if (isBuffering){
            Iterator<VoiceFrame> iter=listBuffer.iterator();
            // an elemente is present since I just made an add
            long currentTimestamp=listBuffer.first().getTimestamp();
            //out("current "+currentTimestamp);
            // ignore first element
            //iter.next();
            // I have at leas 20ms in the buffer
            int bufferedMillis=0;
            while(iter.hasNext()){
                VoiceFrame next=iter.next();
                long nextstamp=next.getTimestamp();
                //out("next "+nextstamp);
                if (nextstamp==currentTimestamp){
                    // next packet referst to istant t+20
                    currentTimestamp=nextstamp+20;
                    bufferedMillis+=20;
//                    if (DEBUG) out("Buffered "+bufferedMillis+" millis" );
                    if(bufferedMillis>=minBufferedMillis){
                        // I have enought consecutive voice frame 
                        // in the buffer to start playback
                        isBuffering=false;
                        isPlaying=true;
                        if (timer==null){
                            timer=new Timer("Playout Timer", true);
                            // play a packet every 20 ms
                            //timer.scheduleAtFixedRate(periodicPopper, 0, 20);
                        }
                        if(DEBUG){
                            out("-------Buffering Complete");
                        }
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
    
    public synchronized long getLowerTimestamp(){
        return listBuffer.first().getTimestamp();
    }
    
    public synchronized long getHigherTimestamp(){
        return listBuffer.last().getTimestamp();
    }
    
    private synchronized VoiceFrame remove(){
        if (isEmpty()){
            // nothing to play or remove
            isPlaying=false;
            isBuffering=true;
            return null;
        }
        VoiceFrame older=listBuffer.first();
        listBuffer.remove(older);
        return older;
    }

    public int setMinBufferedMillis(int n){
        if (minBufferedMillis>=0)
            minBufferedMillis=n;
        return minBufferedMillis;
    }
    
    public synchronized int size(){
        return listBuffer.size();
    }

    public synchronized boolean isEmpty(){
        return listBuffer.isEmpty();
    }

    class Deliver extends TimerTask {
        
        private int samplesPlayed;
        private long nextTimestampToPlay;
        private boolean first=true;

        public void run() {
//            while(isPlaying){
//                if (first){
//                    
//                }
//                
//            }
            
            if (isPlaying){
                if(isEmpty()){
                    isPlaying=false;
                    isBuffering=true;
                    // Notify decoder of the PL
                    decoder.decodeFrame(null);
                    return;
                }
//                out("lower"+getLowerTimestamp());
//                out("next"+nextTimestampToPlay);
                // if a packet is missing it meeans that getLowerTimestamp()
                // is higher than nextTimestampToPlay
                // if it is lower it means that I'm introducing unnecessary 
                // delay
                if (getLowerTimestamp()<nextTimestampToPlay)
                    nextTimestampToPlay=getLowerTimestamp();
                // I want to have bounded delay, max acceptable delay is 
                // maxBufferedMillis
                // max delay should be managed by add method
                if (getHigherTimestamp()>maxBufferedMillis+nextTimestampToPlay)
                    nextTimestampToPlay=getLowerTimestamp();
                if (getLowerTimestamp()==nextTimestampToPlay){
                    samplesPlayed++;
                    // in the buffer there is what i want to play
                    //send to the decoder
                    VoiceFrame vf=remove();
                    decoder.decodeFrame(vf.getContent());
                }
                else{
                    //notify the decoder of the problem (PL?)
                    decoder.decodeFrame(null);
                }
                nextTimestampToPlay+=20;
            }
            else{
                //synchronize while not playing to first playable sample
                if (!isEmpty()){
                    nextTimestampToPlay=getLowerTimestamp();
                   // isPlaying=true;
                }
            }
        }
    }//Deliver
    
   
    class VoiceFrameComparator implements Comparator{

        public int compare(Object o1, Object o2) {
            VoiceFrame f1= (VoiceFrame) o1;
            VoiceFrame f2= (VoiceFrame) o2;
            return (int)(f1.getTimestamp()-f2.getTimestamp());
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
