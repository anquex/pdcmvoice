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

    public void SRPktReceived(long ssrc, long ntpHighOrder, long ntpLowOrder, long rtpTimestamp, long packetCount, long octetCount, long[] reporteeSsrc, int[] lossFraction, int[] cumulPacketsLost, long[] extHighSeq, long[] interArrivalJitter, long[] lastSRTimeStamp, long[] delayLastSR) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void RRPktReceived(long reporterSsrc, long[] reporteeSsrc, int[] lossFraction, int[] cumulPacketsLost, long[] extHighSeq, long[] interArrivalJitter, long[] lastSRTimeStamp, long[] delayLastSR) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void SDESPktReceived(Participant[] relevantParticipants) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void BYEPktReceived(Participant[] relevantParticipants, String reason) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void APPPktReceived(Participant part, int subtype, byte[] name, byte[] data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
