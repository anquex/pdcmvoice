/*
 * To change this template; choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.impl;

import pdcmvoice.util.AverageContainer;

/**
 *
 * @author marco
 */
public class RTCPStats {

    // source Reports
    public long SRssrc;
    public long SRntpHighOrder;
    public long SRntpLowOrder;
    public long SRrtpTimestamp;
    public long SRpacketCount;
    public long SRoctetCount;
    public long[] SRreporteeSsrc;
    public int[] SRlossFraction; // multiplied by 256
    public int[] SRcumulPacketsLost; // total packets lost
    public long[] SRextHighSeq;
    public long[] SRinterArrivalJitter;
    public long[] SRlastSRTimeStamp;
    public long[] SRdelayLastSR;

    // Receiver Reports

    public long RRreporterSsrc;
    public long[] RRreporteeSsrc;
    public int RRlossFraction;
    public int RRcumulPacketsLost;
    public long[] RRextHighSeq;
    public long RRinterArrivalJitter;
    public long[] RRlastSRTimeStamp;
    public long RRdelayLastSR;

    // AVG (RR based)

    // how many reports use to calculate average
    public int reportsAVG=3;
    public int minReports=3;

    AverageContainer jitterAVG= new AverageContainer(reportsAVG,minReports);
    private AverageContainer plossAVG = new AverageContainer(reportsAVG,minReports);;

    // counters

    private int receivedRR;
    private int receiverSR;

    public void SRPktReceived(long ssrc, long ntpHighOrder, long ntpLowOrder, long rtpTimestamp, long packetCount, long octetCount, long[] reporteeSsrc, int[] lossFraction, int[] cumulPacketsLost, long[] extHighSeq, long[] interArrivalJitter, long[] lastSRTimeStamp, long[] delayLastSR){

        receiverSR++;

        SRssrc=ssrc;
        SRntpHighOrder=ntpHighOrder;
        SRntpLowOrder=ntpLowOrder;
        SRrtpTimestamp=rtpTimestamp;
        SRpacketCount=packetCount;
        SRoctetCount=octetCount;
        SRreporteeSsrc=reporteeSsrc;
        SRlossFraction=lossFraction;
        SRcumulPacketsLost=cumulPacketsLost;
        SRextHighSeq=extHighSeq;
        SRinterArrivalJitter=interArrivalJitter;
        SRlastSRTimeStamp=lastSRTimeStamp;
        SRdelayLastSR=delayLastSR;

    }

    public void RRPktReceived(long reporterSsrc, long[] reporteeSsrc, int[] lossFraction, int[] cumulPacketsLost, long[] extHighSeq, long[] interArrivalJitter, long[] lastSRTimeStamp, long[] delayLastSR) {
        if (lossFraction==null){
            System.out.println("This should not happen!!!!");
            return;
        }
        receivedRR++;
        RRreporterSsrc=reporterSsrc;
        RRreporteeSsrc=reporteeSsrc;
        if(lossFraction.length>0){
            RRlossFraction=Math.abs(lossFraction[0]);
            plossAVG.add(RRlossFraction);
        }
        if(cumulPacketsLost.length>0)
            RRcumulPacketsLost=cumulPacketsLost[0];
        RRextHighSeq=extHighSeq;
        if(interArrivalJitter.length>0)
            RRinterArrivalJitter=Math.abs(interArrivalJitter[0]);
        RRlastSRTimeStamp=lastSRTimeStamp;
        if(delayLastSR.length>0)
            RRdelayLastSR=delayLastSR[0];
    }

    public int getAveragePloss(){
        return plossAVG.getAverage();
    }

    public int getAverageJitter(){
        return jitterAVG.getAverage();
    }
    
    public AverageContainer getAvgJitterCont(){
        return jitterAVG;
    }
    public AverageContainer getAvgPlossCont(){
        return plossAVG;
    }

}//RTCTStats
