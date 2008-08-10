/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.impl;

import java.util.logging.Level;
import java.util.logging.Logger;
import jlibrtp.Participant;
import jlibrtp.RTCPAppIntf;
import static pdcmvoice.impl.Constants.*;

/**
 *  Optimize Voice Session Settings to achive best performance.
 *  optimization is done by calling voicesession parent methods to
 *  set values
 *
 * @author marco
 */
public class VoiceSessionController extends Thread implements RTCPAppIntf {

    // parent voice session

    private VoiceSession parent;

    // stats container

    private RTCPStats rtcpStats;

    // counters

    private int receivedRR;
    private int receiverSR;

    // minimum number of Receiver Reports required
    // before starting calculating averages
    public final static int MIN_RR_REQUIRED=3;

    public final static int WAIT_FOR_OPTIMIZE=30000;

    //paused status, Controller pause optimisation
    private boolean paused=!DEFAULT_DYNAMIC_ADAPTATION;

    // causes process to die
    private boolean doTerminate=false;

    private boolean maxBufferOptimizationRunning;


    public VoiceSessionController(VoiceSession parent){
        this.parent=parent;
        rtcpStats=new RTCPStats();
    }

    /**
     *
     */

    public void run(){

        //initial wait cycle

        while (receivedRR<MIN_RR_REQUIRED){
            try {
                sleep(1000);
            } catch (InterruptedException ex) {}
        }

        // I've received enought data
        // Start updating average

        while(!doTerminate){
            if (!paused){
                    setBest();
                try {
                    sleep(WAIT_FOR_OPTIMIZE);
                } catch (InterruptedException ex) {
                    out("Ho dormito male e mi tocca anche lavorare :(");
                }
            }
            else{
                try {
                    sleep(1000);
                } catch (InterruptedException ex) {}
            }
        }// end while
    }// end run

    /**
     *  Action to be taken to set best
     */

    private void setBest(){
        // Optimize playout buffer
        //out("doing my best");
        int avg=rtcpStats.getAverageJitter();
        out(avg);
        if(avg!=-1){
            //out("Jitter medio "+avg);
            parent.setMinBufferedMillis(Math.max(60, avg*3));
        }
        // Optimize FEC/Recovery
        avg=rtcpStats.getAveragePloss();
        if(avg!=-1){
            if (avg>(2*256))
                parent.RDT(true);
            else
                parent.RDT(false);
        }

    }

    public void terminate(){
        doTerminate=true;
    }

    public void pauseOptimizing(){
        paused=true;
    }

    public void continueOptimizing(){
        paused=false;
    }

    public void SRPktReceived(long ssrc, long ntpHighOrder, long ntpLowOrder, long rtpTimestamp, long packetCount, long octetCount, long[] reporteeSsrc, int[] lossFraction, int[] cumulPacketsLost, long[] extHighSeq, long[] interArrivalJitter, long[] lastSRTimeStamp, long[] delayLastSR) {
        receiverSR++;
        rtcpStats.SRPktReceived(ssrc, ntpHighOrder, ntpLowOrder, rtpTimestamp, packetCount, octetCount, reporteeSsrc, lossFraction, cumulPacketsLost, extHighSeq, interArrivalJitter, lastSRTimeStamp, delayLastSR);
    }

    public void RRPktReceived(long reporterSsrc, long[] reporteeSsrc, int[] lossFraction, int[] cumulPacketsLost, long[] extHighSeq, long[] interArrivalJitter, long[] lastSRTimeStamp, long[] delayLastSR) {
        receivedRR++;
        rtcpStats.RRPktReceived(reporterSsrc, reporteeSsrc, lossFraction, cumulPacketsLost, extHighSeq, interArrivalJitter, lastSRTimeStamp, delayLastSR);
        parent.updateSessionPercivedPloss();
        rtcpStats.getAvgJitterCont().add(parent.myJitter());
    }

    public void SDESPktReceived(Participant[] relevantParticipants) {

        // JUST TO SEE IF SOME RR IS GENERATED... (SEEMS NO...)
//        for (int i=0;i<relevantParticipants.length;i++){
//            Participant p= relevantParticipants[i];
//            rtpSession.addParticipant(p);
//        }
        
    }

    public void BYEPktReceived(Participant[] relevantParticipants, String reason) {
        
        //parent.stop();
    }

    public void APPPktReceived(Participant part, int subtype, byte[] name, byte[] data) {
        //DO NOTHING
    }

    public RTCPStats getRTCPStats(){
        return rtcpStats;
    }

    public boolean isPaused(){
        return paused;
    }

}// End VoiceSessionController
