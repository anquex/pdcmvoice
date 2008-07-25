/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.impl;

import jlibrtp.Participant;
import jlibrtp.RTCPAppIntf;

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

    public VoiceSessionController(){
        rtcpStats=new RTCPStats();
    }
    public void SRPktReceived(long ssrc, long ntpHighOrder, long ntpLowOrder, long rtpTimestamp, long packetCount, long octetCount, long[] reporteeSsrc, int[] lossFraction, int[] cumulPacketsLost, long[] extHighSeq, long[] interArrivalJitter, long[] lastSRTimeStamp, long[] delayLastSR) {
        rtcpStats.SRPktReceived(ssrc, ntpHighOrder, ntpLowOrder, rtpTimestamp, packetCount, octetCount, reporteeSsrc, lossFraction, cumulPacketsLost, extHighSeq, interArrivalJitter, lastSRTimeStamp, delayLastSR);
    }

    public void RRPktReceived(long reporterSsrc, long[] reporteeSsrc, int[] lossFraction, int[] cumulPacketsLost, long[] extHighSeq, long[] interArrivalJitter, long[] lastSRTimeStamp, long[] delayLastSR) {
        //DO NOTHING
    }

    public void SDESPktReceived(Participant[] relevantParticipants) {
        //DO NOTHING
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
