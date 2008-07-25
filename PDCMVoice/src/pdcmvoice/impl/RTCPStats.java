/*
 * To change this template; choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.impl;

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
    public int[] RRlossFraction;
    public int[] RRcumulPacketsLost;
    public long[] RRextHighSeq;
    public long[] RRinterArrivalJitter;
    public long[] RRlastSRTimeStamp;
    public long[] RRdelayLastSR;

    public RTCPStats(){
    }

    public void SRPktReceived(long ssrc, long ntpHighOrder, long ntpLowOrder, long rtpTimestamp, long packetCount, long octetCount, long[] reporteeSsrc, int[] lossFraction, int[] cumulPacketsLost, long[] extHighSeq, long[] interArrivalJitter, long[] lastSRTimeStamp, long[] delayLastSR){

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

    RRreporterSsrc=reporterSsrc;
    RRreporteeSsrc=reporteeSsrc;
    RRlossFraction=lossFraction;
    RRcumulPacketsLost=cumulPacketsLost;
    RRextHighSeq=extHighSeq;
    RRinterArrivalJitter=interArrivalJitter;
    RRlastSRTimeStamp=lastSRTimeStamp;
    RRdelayLastSR=delayLastSR;
    }

}
