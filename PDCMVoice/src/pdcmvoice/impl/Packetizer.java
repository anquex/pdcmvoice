/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.impl;

import jlibrtp.RTPSession;
import static pdcmvoice.impl.Constants.*;
import pdcmvoice.recovery.RecoveryCollection;

/**
 * @author marco
 */

public class Packetizer {

    private RTPSession rtpSession;
    private byte[] previousEncodedFrame;
    private boolean RDT;            // Rendundant Data Transmission Enable?
    private int initialPayloadType; // initial RTP payloadtype
    private int framesPerPacket;    //how much 20ms voice per packet?
    private boolean isFirst;
    private long currentTimeStamp;

    // negative value used to see it is not valid
    private long firstFrameTimestamp=-1;
    private long firstPacketTimestamp=-1;
    private int lastPacketSN=-1;
    private boolean lastPacketwasRDT;
    private int lastFramesPerPacket=-1;
    private int lastAudioFrameSize=-1;
    private int lastPacketPayload=-1;

    // start supposing not having voice frames waiting to be sent
    private boolean lastEncodedHasBeenSent=true;

    private final int DEFAULT_FRAMES_PER_PACKET=ALLOWED_FRAMES_PER_PACKET[DEFAULT_FRAMES_PER_PACKET_INDEX];

    private static final boolean DEBUG=false;
    private static final boolean VERBOSE=false;
    int n;


    //RECOVERY COLLECTION VARIABLES
    private RecoveryCollection local;

    public Packetizer(RTPSession s){
        rtpSession= s;
        System.out.println("Initial RTP PAYLOAD :"+rtpSession.payloadType());
        initialPayloadType=rtpSession.payloadType();
        RDT=isSessionRTD(initialPayloadType);
        isFirst=true;
        framesPerPacket= DEFAULT_FRAMES_PER_PACKET;
    }


    public Packetizer(RTPSession s, RecoveryCollection local){
        this(s);
        this.local = local;
    }


    public synchronized void sendVoice(byte[] currentEncodedFrame){
        // generate timestamp the first time and update the other times
        updateTimestamp();

        if(rtpSession.isEnding()) return;
            //don't send packets if rtp session (and of course sockets)
            // have been closed
        if (RDT && lastEncodedHasBeenSent){
            /* if I switch from (2 frames per packet) -> (RDT)
             * I could have a frame not be sent, so I just
             *  continue with nonRDT for currentframe
             */

            // RDT only 1 new audio frame per packet
            framesPerPackets(1);


        /* ---------------------
         * ---  RDT PACKETS  ---
         * ---------------------*/

            //for the first audio frame
            if(previousEncodedFrame==null){
                // this happens only for the session's first encoded frame
                // RDT packets expects 2 audio frames but I have only one

                // Send this packet as a non RDT session
                rtpSession.payloadType(getNotRDTPayloadType(initialPayloadType));
                // send as non marked
                sendFrames(currentEncodedFrame, false);
                //update rtpsession Payload, RDT payload Enabled
                rtpSession.payloadType(getRDTPayloadType(initialPayloadType));
            }
            else{// other RDT frames
                 // get a packet with last 2 encoded frames
                
//                if (n %2==0){
//                    n=1;
//                } else{
                rtpSession.payloadType(getRDTPayloadType(initialPayloadType));

                byte[] frame=forgeBigFrame(currentEncodedFrame, previousEncodedFrame);
                // set RDT values???
                sendFrames(frame, false);
//                    n=0;
//                }
            }

            // RDT sessions sends packets each sendVoice call
            lastEncodedHasBeenSent=true;

        }else{

        /* ---------------------
         * ---NON RDT PACKETS---
         * ---------------------*/

            // set the payload NOT RDT
            rtpSession.payloadType(getNotRDTPayloadType(initialPayloadType));

            // 1 frame per packet
            // if I have still a packet waiting to be sent
            // i continue using 2 frames  solution
            if (framesPerPacket==1 && lastEncodedHasBeenSent){
                sendFrames(currentEncodedFrame, false);
                lastEncodedHasBeenSent=true;
            }
            else{
            // 2 frames per packet
                if(!lastEncodedHasBeenSent){
                   if (DEBUG){
                        if(isRDT() || framesPerPacket==1){
                            // I'm here just because I need to finish
                            // before chancing mode
                            String out="A frame was waiting... Delaying mode " +
                                    "switch of 1 packet...";
                            out(out);
                        }
                   }
                   byte[] frame=forgeBigFrame(currentEncodedFrame, previousEncodedFrame);
                   sendFrames(frame, true);
                   lastEncodedHasBeenSent=true;
                }
                else{
                    // simply wait for two frames to be ready
                    lastEncodedHasBeenSent=false;
                }
            }
        }
        previousEncodedFrame=currentEncodedFrame;
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

    private int getNotRDTPayloadType(int sessionPayload) {
        if(sessionPayload==PAYLOAD_SPEEX || sessionPayload==PAYLOAD_SPEEX_RDT)
            return PAYLOAD_SPEEX;
        if(sessionPayload==PAYLOAD_iLBC || sessionPayload==PAYLOAD_iLBC_RDT)
            return PAYLOAD_iLBC;
        throw new IllegalArgumentException("Unknown payload type");
    }

    private static boolean isSessionRTD(int sessionPayload) {
        if(sessionPayload==PAYLOAD_SPEEX_RDT||
           sessionPayload==PAYLOAD_iLBC_RDT)
            return true;
        else return false;
    }

    private long[][] sendFrames(byte[] frames, boolean marked){
        byte[][] f= new byte[1][1];
        boolean[] markers= new boolean[1];
        markers[0]=marked;
        f[0]=frames;
        if(firstPacketTimestamp==-1)
            firstPacketTimestamp=currentTimeStamp;
        long[][] r=rtpSession.sendData(f, null, markers, currentTimeStamp, null);
        lastPacketSN=(int) r[0][1];
        lastPacketwasRDT=isRDT();
        lastPacketPayload=rtpSession.payloadType();
        if(isRDT() || framesPerPackets()==2){
            lastFramesPerPacket=2;
            lastAudioFrameSize=frames.length/2;
        }else{
            lastAudioFrameSize=frames.length;
            lastFramesPerPacket=framesPerPackets();
        }

        /* ---------------------------------------
         * --- RECOVERY COLLECTION CODE BEGINS ---
         * ---------------------------------------*/

        // collection.add((int)r[1],f, r[0]);
        if (local!=null){
            //this means recovery mode is active

            byte[] toSend = new byte[local.getPktSize()];
            System.arraycopy(frames, 0, toSend, 0, local.getPktSize()); //singolo pacchetto voce: 20Byte
            this.local.add((int)r[0][1], toSend, r[0][0]);

//           if (marked)
//            {
//                System.arraycopy(frames, local.getPktSize(), toSend, 0, local.getPktSize());
//                this.local.add((int)r[1][1], toSend, r[1][0]);
//            }
        }
        /* ---------------------------------------
         * --- RECOVERY COLLECTION CODE ENDS   ---
         * ---------------------------------------*/
        if (DEBUG){
            String out="";
            out+="Sending Packet with";
            out+=" PAYLOAD: "+rtpSession.payloadType();
            out+=" AUDIO :"+frames.length;
            out+=" MARKED :"+marked;
            out+=" TIMESTAMP :"+currentTimeStamp;
            out+=" SN :"+r[0][1];
            if (VERBOSE){
                out+="\n   Content: ";
                for (int i=0;i<frames.length;i++){
                    out+=" "+frames[i];
                }
            }
            out(out);
        }
        return r;
    }

    private static byte[] forgeBigFrame(byte[] currEncoded,byte[] prevEncoded){
            int lenght=currEncoded.length;
            byte[] f=new byte[lenght*2];
            System.arraycopy(currEncoded, 0, f, 0, lenght);
            System.arraycopy(prevEncoded, 0, f, lenght, lenght);
            return f;
    }

    private void updateTimestamp(){
        if (isFirst){
            // get only last least significant 32 bit
            currentTimeStamp=(long) (System.currentTimeMillis() % Math.pow(2, 32));
            isFirst=false;
        }
        else{
            currentTimeStamp+=20; //a frame is generated every 20ms
        }

        // save first timestamp
        if (firstFrameTimestamp==-1)
            firstFrameTimestamp=currentTimeStamp;
    }

    public int framesPerPackets(int n){
        if (n==1)
            framesPerPacket=1;
        if(n==2 && !isRDT())
            framesPerPacket=2;
        return framesPerPacket;
    }

    public int framesPerPackets(){
        return framesPerPacket;
    }

    public long getFirstFrameTimestamp(){
        // -1 means never updated
        return firstFrameTimestamp;
    }
    public long getFirstPacketTimestamp(){
        // -1 means never updated
        return firstPacketTimestamp;
    }

    private void updateLastPacketInfo(){

    }
    public long lastPacketSN(){
        return lastPacketSN;
    }
    public boolean lastPacketRDT(){
        return lastPacketwasRDT;
    }
    public int lastPacketFrames(){
        return lastFramesPerPacket;
    }
    public int lastPacketFramesSize(){
        return lastAudioFrameSize;
    }

    public int lastPacketPayload(){
        return lastPacketPayload;
    }
}
