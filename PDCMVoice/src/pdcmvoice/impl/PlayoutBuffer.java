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

import java.util.logging.Level;
import java.util.logging.Logger;
import static pdcmvoice.impl.Constants.*;

/**
 *
 * @author marco
 */
public class PlayoutBuffer{

    private boolean isFirstBuffering;          //is true if I'm buffering for the first time
    private long startFrameTimestamp=Long.MAX_VALUE;
    private boolean isBuffering;      //true if buffer is waiting to fill up
    private Decoder decoder;
    private int minBufferedMillis=DEFAULT_MIN_BUFFER_SIZE;
    // if buffer is empty I wait for
    // minBufferedMillis before starting
    // playback

    // must be at least 20 ms!
    private int maxBufferedMillis=DEFAULT_MAX_BUFFER_SIZE;
                                      // if buffer exeeds maxBufferedMillis
                                      // I start dropping oleder frames

    private SortedSet<VoiceFrame> listBuffer; // frame container sorted by time
                                              // stamp

    private Timer timer;            // popout a frame every 20ms
    private Deliver decoderDeliver; // does the pop out work
    private BrustKiller brustKiller;

    private final boolean DEBUG=true;

    //CONSTANTS

    public final int TIME_PER_FRAME = 20; // in ms
    public final boolean WANTCONSECUTIVE=true;

    // STATS VARIABLES
    
    private int totalAdded;
    private long higherReceivedTimestamp=-1;
    private long lastIntervalTimestamp=0;
    private int previousTotalAdded;
    private float intervalPloss;
    private float sessionPloss;

    // prova
    private long[] lastSeenTimestamps=new long[256];


    public PlayoutBuffer() {
//        listBuffer=Collections.synchronizedSortedSet(new TreeSet<VoiceFrame>(new VoiceFrameComparator()));
        listBuffer=new TreeSet<VoiceFrame>(new VoiceFrameComparator());
        isFirstBuffering=true;
        isBuffering=true;
        decoderDeliver= new Deliver();
//        // send a frame to decoder each 20 ms
//        timer=new Timer("Playout Buffer Timer");
//        //timer.schedule(decoderDeliver,0,20);
//        timer.scheduleAtFixedRate(decoderDeliver,0,20);
        brustKiller=new BrustKiller();
        brustKiller.start();
    }

    public void registerDecoder(Decoder d){
        decoder=d;
    }

    /**
     *  Add a 20ms encoded audio frame to playout buffer
     *
     * @param timestamp
     * @param frame
     */


    public synchronized void add(long timestamp, byte[] frame){

        // ricordo gli ultimi 256 frame ed evito di aggiungere
        // va bene fino a 5 secondi di jitter
        int index=(int) (timestamp/20) %256;
        if(lastSeenTimestamps[index]==timestamp){
            return;
        }else{
            lastSeenTimestamps[index]=timestamp;
        }
        // protect playout from late frames
        if (timestamp<decoderDeliver.getNextTimestampToPlay()){
            //packet arrived too late!
            // ignore it
            out("BUFFER: OUT OF TIME... frame "+timestamp+" dropped" +
                    " (Should already been played)");
            return;
        }

        VoiceFrame v=new VoiceFrame(timestamp, frame);
        if(listBuffer.add(v)){
            totalAdded++;
            higherReceivedTimestamp=Math.max(higherReceivedTimestamp, timestamp);
        }

        if (DEBUG) out("BUFFER: Frame "+timestamp+" Added : new size... "+size());

        if (isFirstBuffering){
            if(startFrameTimestamp>timestamp){
                startFrameTimestamp=timestamp;
                if (DEBUG) out("BUFFER: First Frame recived: First timestamp "
                               +startFrameTimestamp);
            }
        }

        if (isBuffering()){
            int bufferedMillis=0; // millis in buffer
            if(isFirstBuffering && WANTCONSECUTIVE ){

                    // I want consecutive frame to get bounded delay
                    Iterator<VoiceFrame> iter=listBuffer.iterator();

                    // at least 1 element is present since I just made an add
                    long currentExpectedTimestamp=getLowerTimestamp();

                    // count how many millis I have in the buffer
                    while(iter.hasNext()){

                        VoiceFrame next=iter.next();

                        long nextstamp=next.getTimestamp();

                        if (nextstamp==currentExpectedTimestamp){
                            // consecutive yes!
                            bufferedMillis+=TIME_PER_FRAME;

                            // next packet refers to istant t+TIME_PER_FRAME
                            currentExpectedTimestamp=nextstamp+TIME_PER_FRAME;

                            //if (DEBUG) out("BUFFER : Buffered "+bufferedMillis+" millis" );

                            if(bufferedMillis>=getMinBufferedMillis()){
                                // I have enought consecutive voice frame
                                // in the buffer to start playback
                                if (DEBUG) out("Packets are consecutive, nice :) ");
                                break;
                            }
                            else{
                                /**
                                 * 
                                 * continuo a bufferizzare mentre il brustkiller si occupa di eliminare 
                                 * i frame + vecchi. in modo da dare la possibilità di avere una sequenza
                                 * consecutiva
        //                       *
                                 */
                                
                                //if(DEBUG)
        //                       out("Still Buffering...");
                            }
                        }else{
                            // i pacchetti non sono consecutivi
                            checkAndFixBrusts();
                            break;
                        }
                    }
            }else{
                bufferedMillis=size()*TIME_PER_FRAME;
            }

            if(bufferedMillis>=getMinBufferedMillis()){
                    isFirstBuffering=false;
                    isBuffering=false;

                    if(DEBUG)  out("-------   Buffering Complete   --------");

                    // send a frame to decoder each 20 ms
                    if(timer==null){
                        timer=new Timer("Playout Buffer Timer");
                       //timer.schedule(decoderDeliver,0,20);
                        timer.scheduleAtFixedRate(decoderDeliver,0,20);
                    }

                    // start playing from older frame
                    decoderDeliver.startPlaying(getLowerTimestamp());
            }// end buffering complete
        }// end isBuffering
    }// end add

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
        int expecetedMax=minBufferedMillis+MAX_MIN_BUFFER_PLUS;
        if(getMaxBufferedMillis()<expecetedMax)
            setMaxBufferedMillis(expecetedMax);
        return minBufferedMillis;
    }

    public synchronized int getMinBufferedMillis(){
        return minBufferedMillis;
    }

    public synchronized int getMaxBufferedMillis(){
        return maxBufferedMillis;
    }
    public synchronized int setMaxBufferedMillis(int n){
        int i= Math.max(n, minBufferedMillis);
        return maxBufferedMillis=i;
    }

    public synchronized  int size(){
        return listBuffer.size();
    }

    public synchronized int getBufferedMillis(){
        return size()*TIME_PER_FRAME;
    }

    public synchronized  boolean isEmpty(){
        return listBuffer.isEmpty();
    }

    public synchronized boolean isBuffering(){
        return isBuffering;
    }

    /**
     *  Remove a frame if latency is too high
     */

    public synchronized void checkAndFixBrusts(){
            // bound max delay

           if(size()<=1) return;

            if(getHigherTimestamp()-getLowerTimestamp()+TIME_PER_FRAME>=getMaxBufferedMillis()
                    //&& !isBuffering
                    )
            {
                if (DEBUG)
                        out("PLAYOUT BUFFER : Maximum Delay Reached: "+
                        (getHigherTimestamp()-getLowerTimestamp()+TIME_PER_FRAME));
                
                int removed=0;
                // drop the older packets
                remove();
                while(getHigherTimestamp()-getLowerTimestamp()+TIME_PER_FRAME>=getMaxBufferedMillis()){
                    remove();
                    removed++;
                }
                out("PLAYOUT BUFFER : "+removed+" Packet(s) Dropped due to High Latency");

                // start playing from the older packet
                decoderDeliver.startPlaying(getLowerTimestamp());
            }
    }// checkAndFixBrusts

    class Deliver extends TimerTask {

        private int samplesPlayed;
        private long nextTimestampToPlay=-1;
        private boolean first=true;
        private boolean isPlaying=true;

        public synchronized void startPlaying(long firstTimestamp){
            if (DEBUG)
                if(first){
                    out ("DELIVER: Start Playing from frame "+firstTimestamp);
                    first=false;
                }else{
                    out ("DELIVER: Continue playing from frame "+firstTimestamp+
                         " ( Skipped "+(firstTimestamp-nextTimestampToPlay)+
                         " ms)");
                }
            nextTimestampToPlay=firstTimestamp;
            isPlaying=true;
        }
        public synchronized  void stopPlaying(){
            out ("DELIVER: Stop  Playing");
            isPlaying=false;
            nextTimestampToPlay=-1;
            first=true;
        }

        public synchronized  long getNextTimestampToPlay(){
            return nextTimestampToPlay;
        }

        public synchronized  boolean isPlaying(){
            return isPlaying;
        }

        //questo metodo sincronizzato provoca deadlock
        public  void run() {
            byte[] audio=null;
            if (isPlaying()){
                synchronized(PlayoutBuffer.this){
                    if(isEmpty()){
                        if (DEBUG) out("BUFFER : Buffer Empty");
                        // nothing to play
                        isBuffering=true; //Playout buffer
                       // stop playing since I don't have nothing to play
                        stopPlaying();
                    }
                    else{
                        // this happens if speakers runs faster than
                        // microphone... just wait
                        if(higherReceivedTimestamp==nextTimestampToPlay && size()>1){
                            if(DEBUG) out("DELIVER: Player running too fast? Waiting a frame");
                            return;
                        }
                        if (getLowerTimestamp()==nextTimestampToPlay){
                            samplesPlayed++;
                            // in the buffer there is what i want to play
                            //send to the decoder
                            audio=remove().getContent();
                            if (DEBUG) out("DELIVER: Playing Frame : "+nextTimestampToPlay);
                        }
                        else{
                            //notify the decoder of the problem (PL?)
                            if (DEBUG){
                                String out="DELIVER: Packet loss , " +
                                           "expeted Frame :"+nextTimestampToPlay;

                               boolean received= lastSeenTimestamps[(int)((nextTimestampToPlay/20) %256)]==nextTimestampToPlay;
                                out+=" filter says it was received? "+received;
                                out(out);
                            }
                        }
                        nextTimestampToPlay+=TIME_PER_FRAME;
                    }
                }//synchronize
            }// is playing
            // mando sempre in ogni caso l'audio al decoder, l'audio potra quindi essere audio vero oppure
            // null per indicare perdita
            decoder.decodeFrame(audio);
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

    public float sessionPloss(){
        return sessionPloss;
    }

    public  void updateSessionPloss(){
        if (totalAdded==0)
            return;
        float expected=(higherReceivedTimestamp-startFrameTimestamp)/TIME_PER_FRAME+1;
        //
        //out("Expected "+expected+" received "+totalAdded);
        sessionPloss=(expected-totalAdded)/expected;
    }

    public float intervalPloss(){
        return intervalPloss;
    }

    public  void updateIntervalPloss(){
        lastIntervalTimestamp=Math.max(lastIntervalTimestamp, startFrameTimestamp);
        float expected= ((higherReceivedTimestamp - lastIntervalTimestamp) / TIME_PER_FRAME);
        intervalPloss=(expected-(totalAdded-previousTotalAdded))/expected;
        lastIntervalTimestamp=higherReceivedTimestamp;
        previousTotalAdded=totalAdded;
    }

    class BrustKiller extends Thread{

        private boolean doTerminate;

        public void run(){

            if(DEBUG) out("BRUST KILLER started...");
            
           while(!doTerminate){
                try {
                    checkAndFixBrusts();
                    // se sto bufferizzando (quindi la riproduzione è interrotta
                    // svuoto il più possibile
                    if(!isBuffering())
                        sleep(1000);
                    else
                        sleep(20);
                } catch (InterruptedException ex) {
                    if(DEBUG) out("BRUST KILLER died!");
                    break;
                }
            }//while
        }//run

        public void terminate(){
            doTerminate=true;
        }
    }// BrustKiller

    public synchronized void stop(){
        if(timer!=null)
            timer.cancel();
        if(brustKiller!=null)
            brustKiller.terminate();
    }

}//Playout Buffer
