/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.impl;

import jlibrtp.RTPSession;
import static pdcmvoice.impl.Constants.*;

/**
 * @date  
 * @author marco
 */
public class Packetizer {
    
    private RTPSession rtpSession;
    private byte[] previousEncodedFrame;
    private boolean RDT;
    private int initialPayloadType;
    
    public Packetizer(RTPSession s){
        rtpSession= s;
        System.out.println("RTP PAYLOAD"+rtpSession.payloadType());
        initialPayloadType=rtpSession.payloadType();
        RDT=isSessionRTD(initialPayloadType);
    }
    
    
    public synchronized void sendVoice(byte[] currentEncodedFrame){
        long[] returnValues;
        if (RDT){
            //the first audio frame
            if(previousEncodedFrame==null){
                // send immidiatly without introducing further delay
                returnValues=rtpSession.sendData(currentEncodedFrame);
                //update rtpsession Payload
                rtpSession.payloadType(getRDTPayloadType(initialPayloadType));
            }
            else{//other frames
                // forge the packet
                int lenght=previousEncodedFrame.length;
                byte[] RDTFrame=new byte[lenght*2];
                System.arraycopy(currentEncodedFrame, 0, RDTFrame, 0, lenght);
                System.arraycopy(previousEncodedFrame, 0, RDTFrame, lenght, lenght);
                returnValues=rtpSession.sendData(RDTFrame);
                
                //DEBUG
//                String out="";
//                for (int i=0;i<RDTFrame.length;i++)
//                    out+=RDTFrame[i]+" ";
//                System.out.println(out);
                
            }
                
        }else{
            returnValues=rtpSession.sendData(currentEncodedFrame);
        }
        previousEncodedFrame=currentEncodedFrame;
        long lastSentTimeStamp=returnValues[0];
        long sequenceNumber=returnValues[1];
        
        
    }
    
    public void enableRDT(){
        //already enabled
        if (RDT) return;
        //enable
        RDT=true;
        //backup inital session payloadType
        initialPayloadType=rtpSession.payloadType();
        //RTPsession payloadtype is updated in sendVoice method
        
    }
    public void disableRDT(){
        RDT=false;
        //backup inital session payloadType
        rtpSession.payloadType(initialPayloadType);
    }
    
    public boolean isRDT(){
        return RDT;
    }
    
    private int getRDTPayloadType(int sessionPayload) {
        if(sessionPayload==PAYLOAD_SPEEX || sessionPayload==PAYLOAD_SPEEX_RDT)
            return PAYLOAD_SPEEX_RDT;
        if(sessionPayload==PAYLOAD_iLBC || sessionPayload==PAYLOAD_iLBC_RDT)
            return PAYLOAD_iLBC_RDT;
        throw new IllegalArgumentException("Unknown payload type");    
    }
    
    private boolean isSessionRTD(int sessionPayload) {
        if(sessionPayload==PAYLOAD_SPEEX_RDT||
           sessionPayload==PAYLOAD_iLBC_RDT)
            return true;
        else return false;
    }
    
    
}
