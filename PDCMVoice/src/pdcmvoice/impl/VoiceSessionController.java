/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.impl;

import jlibrtp.Participant;
import jlibrtp.RTCPAppIntf;
import jlibrtp.RTPSession;

/**
 *
 * @author marco
 */
/**
 *
 * @author marco
 */
public class VoiceSessionController implements RTCPAppIntf{

    private RTCPStats rtcpStats;
    private RTPSession rtpSession;

    public VoiceSessionController(RTPSession s){
        rtcpStats=new RTCPStats();
        rtpSession=s;

    }
    public void SRPktReceived(long ssrc, long ntpHighOrder, long ntpLowOrder, long rtpTimestamp, long packetCount, long octetCount, long[] reporteeSsrc, int[] lossFraction, int[] cumulPacketsLost, long[] extHighSeq, long[] interArrivalJitter, long[] lastSRTimeStamp, long[] delayLastSR) {
        rtcpStats.SRPktReceived(ssrc, ntpHighOrder, ntpLowOrder, rtpTimestamp, packetCount, octetCount, reporteeSsrc, lossFraction, cumulPacketsLost, extHighSeq, interArrivalJitter, lastSRTimeStamp, delayLastSR);
    }

    public void RRPktReceived(long reporterSsrc, long[] reporteeSsrc, int[] lossFraction, int[] cumulPacketsLost, long[] extHighSeq, long[] interArrivalJitter, long[] lastSRTimeStamp, long[] delayLastSR) {
        rtcpStats.RRPktReceived(reporterSsrc, reporteeSsrc, lossFraction, cumulPacketsLost, extHighSeq, interArrivalJitter, lastSRTimeStamp, delayLastSR);
    }

    public void SDESPktReceived(Participant[] relevantParticipants) {

        // JUST TO SEE IF SOME RR IS GENERATED... (SEEMS NO...)
//        for (int i=0;i<relevantParticipants.length;i++){
//            Participant p= relevantParticipants[i];
//            rtpSession.addParticipant(p);
//        }
        
    }

    public void BYEPktReceived(Participant[] relevantParticipants, String reason) {
        //DO NOTHING
    }

    public void APPPktReceived(Participant part, int subtype, byte[] name, byte[] data) {
        //DO NOTHING
    }

    public RTCPStats getRTCPStats(){
        return rtcpStats;
    }

}
