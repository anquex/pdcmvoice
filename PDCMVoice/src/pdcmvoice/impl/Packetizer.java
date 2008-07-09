/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.impl;

import jlibrtp.RTPSession;

/**
 * @date  
 * @author marco
 */
public class Packetizer {
    
    private RTPSession rtpSession;
    
    public Packetizer(RTPSession s){
        rtpSession= s;
    }
    
    
    public synchronized void sendVoice(byte[] encodedFrameToSend){
        long[] temp=rtpSession.sendData(encodedFrameToSend);
        long lastSentTimeStamp=temp[0];
        long sequenceNumber=temp[1];
        
    }
}
