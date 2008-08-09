/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.impl;

import jlibrtp.DataFrame;
import jlibrtp.Participant;
import jlibrtp.RTPAppIntf;
import jlibrtp.RTPSession;
import pdcmvoice.recovery.RecoveryCollection;

import static pdcmvoice.impl.Constants.*;


/**
 *
 * @author marco
 */
public class Depacketizer implements RTPAppIntf{

    private final boolean DEBUG=false;

    private RTPSession rtpSession;
    private Decoder decoder;
    private boolean registered;
    private long lastReceivedSN=-1;
    private boolean inited;

    private RecoveryCollection remote;

    PlayoutBuffer playoutBuffer;

    //Last ReceivedFrameInfo
    private boolean lastPacketwasRDT;
    private int lastAudioFrameSize;
    private int lastFramesPerPacket;
    private int lastPacketReceivedPayload;


    public Depacketizer(RTPSession s){
        rtpSession=s;
        playoutBuffer=new PlayoutBuffer();
        // disable rtp buffering, recive all packets!
        rtpSession.packetBufferBehavior(-1);
        // corretto l'errore in jlibrtp che non ritornava i pacchetti
        // se -1 !! (che libreria feccia)


    }

    public Depacketizer(RTPSession s, RecoveryCollection remote){
        this(s);
        /*
         * ATTENZIONE!! ABILITARE LA PROSSIMA ISTRUZIONE SOLO 
         * PER L'ESECUZIONE DI RecoverySystemLoopBackTest.java
         * 
         * //rtpSession.registerRTPSession(this, null, null); (vecchia rtpSession.RTPSessionRegister(this, null, null);)
         */

        // NON ABILITARE, LA REGISTRAZIONE VIENE EFFETTUATA IN VOICE SESSION!!!
        // WARNING !!!!
        //rtpSession.registerRTPSession(this, null, null);
        this.remote = remote;
    }

    public void receiveData(DataFrame frame, Participant participant)
    {
        if (!inited) return;

        //updateLastReceivedFrameInfo(frame);

        /*  ------------------------------
         *  --- SEND TO PLAYOUT BUFFER ---
         *  ------------------------------ */

        byte[] voice=frame.getConcatenatedData();
        int lenght=0;

        if (DEBUG){
            String out="";
            out+="Received Packet with";
            out+=" PAYLOAD: "+ frame.payloadType();
            out+=" AUDIO :"+ voice.length;
            out+=" MARKED :"+ frame.marked();
            out+=" TIMESTAMP :"+ frame.rtpTimestamp();
            out+=" SN :"+ frame.sequenceNumbers()[0];
            out(out);
        }

        /* ---------------------------------------
         * --- RECOVERY COLLECTION CODE BEGINS ---
         * ---------------------------------------*/

//        // collection.add(frame.sequenceNumbers()[0], voice, frame.rtpTimestamp());
//        if (remote!=null){
//            byte[] toSend = new byte[remote.getPktSize()];
//            System.arraycopy(voice, 0, toSend, 0, remote.getPktSize()); //singolo pacchetto voce: 20Byte
//            //ATTENZIONE!!!
//            //SIMULAZIONE PERDITA PACCHETTI
////            if ((int)frame.sequenceNumbers()[0] % 10 != 0)//SIMULAZIONE PERDITA PACCHETTI
//
//            if (((int)frame.sequenceNumbers()[0] < 5
//                ||
//                (int)frame.sequenceNumbers()[0] > 10 && (int)frame.sequenceNumbers()[0] <= 30
//                ||
//                (int)frame.sequenceNumbers()[0] > 40 && (int)frame.sequenceNumbers()[0] <= 60
//                ||
//                (int)frame.sequenceNumbers()[0] > 70 && (int)frame.sequenceNumbers()[0] <= 90
//                ||
//                (int)frame.sequenceNumbers()[0] > 120)
//                    && (int)frame.sequenceNumbers()[0] % 10 != 0)//SIMULAZIONE PERDITA PACCHETTI
//
////            if ((int)frame.sequenceNumbers()[0] % 2 != 0)//SIMULAZIONE PERDITA PACCHETTI
//
//            this.remote.add((int)frame.sequenceNumbers()[0], toSend, frame.rtpTimestamp());
//
//            if (frame.marked())
//            {
//                System.arraycopy(voice, remote.getPktSize(), toSend, 0, remote.getPktSize());
//                //ATTENZIONE!!!
//                //SIMULAZIONE PERDITA PACCHETTI
////                if ((int)frame.sequenceNumbers()[1] % 10 != 0)//SIMULAZIONE PERDITA PACCHETTI
//
//                if (((int)frame.sequenceNumbers()[1] < 5
//                        ||
//                        (int)frame.sequenceNumbers()[1] > 10 && (int)frame.sequenceNumbers()[1] <= 30
//                        ||
//                       (int)frame.sequenceNumbers()[1] > 40 && (int)frame.sequenceNumbers()[1] <= 60
//                       ||
//                       (int)frame.sequenceNumbers()[1] > 70 && (int)frame.sequenceNumbers()[1] <= 90
//                       ||
//                       (int)frame.sequenceNumbers()[1] > 120)
//                               && (int)frame.sequenceNumbers()[1] % 10 != 0) //SIMULAZIONE PERDITA PACCHETTI
//
////                if ((int)frame.sequenceNumbers()[1] % 2 != 0)//SIMULAZIONE PERDITA PACCHETTI
//
//                this.remote.add((int)frame.sequenceNumbers()[1], toSend, frame.rtpTimestamp());
//            }
//        }

        /* ---------------------------------------
         * --- RECOVERY COLLECTION CODE ENDS -----
         * ---------------------------------------*/

        if (isRDT(frame.payloadType()) || frame.marks()[0]){
//            out("2 frame");
            lenght=voice.length/2;
            byte[] v=new byte[voice.length/2];
            byte[] v2=new byte[voice.length/2];
            // first add older packet (see playout buffer why)
            System.arraycopy(voice, lenght, v, 0, lenght);
//            playoutBuffer.add(frame.rtpTimestamp()-20, v);
            new Action(frame.rtpTimestamp()-20, v).start();
            // then add new packet
            
//            out("Frame arrivato "+(frame.rtpTimestamp()-20)+
//                    " byte "+byteToString(v));
            System.arraycopy(voice, 0, v2, 0, lenght);
//            playoutBuffer.add(frame.rtpTimestamp(), v);
            new Action(frame.rtpTimestamp(), v2).start();
            
//            out("Frame arrivato "+frame.rtpTimestamp()+
//                    " byte "+byteToString(v));
        }
        else{
//            out("1 frame");
            //playoutBuffer.add(frame.rtpTimestamp(), voice);
            new Action(frame.rtpTimestamp(), voice).start();
        }

    }
    
    class Action extends Thread{
        // using Action we hope order is mantained
        // usually yes :)
        private byte[]b;
        private long t;

        public Action(long t,byte[] b) {
            this.b = b;
            this.t = t;
        }
        public void run(){
            playoutBuffer.add(t, b);
        }
    }

    // to prevent null pointer exception
    public void init(){
        inited=true;
    }

    private boolean isRDT(int payloadType){
        if (payloadType==PAYLOAD_SPEEX_RDT ||
            payloadType==PAYLOAD_iLBC_RDT)
            return true;
        // unknown payload or not RDT
        else return false;

    }


    public boolean registerDecoder(Decoder d){
        if (d==null) throw new NullPointerException("Null is not a valid Decoder");
        if(registered) {
                System.out.println("Depacketizer: Can\'t register another decoder!");
                return false;
        } else {
                registered = true;
                //System.out.println("Decoder Registered");
                this.decoder=d;
                playoutBuffer.registerDecoder(d);
                return true;
        }
    }
    // NEVER USED
    public void userEvent(int type, Participant[] participant) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    // NEVER USED
    public int frameSize(int payloadType) {
        // 1 packet -> at least 1 frame
        return 1;
    }

    public PlayoutBuffer getPlayoutBuffer() {
        return playoutBuffer;
    }

    public void updateLastReceivedFrameInfo(DataFrame frame){
         lastPacketReceivedPayload=frame.payloadType();
         lastReceivedSN=frame.sequenceNumbers()[0];
         lastPacketwasRDT= isRDT(frame.payloadType());
         if (lastPacketwasRDT || frame.marked()){
             lastAudioFrameSize=frame.getConcatenatedData().length/2;
             lastFramesPerPacket=2;
         }
         else{
             lastAudioFrameSize=frame.getConcatenatedData().length;
             lastFramesPerPacket=1;
         }

    }

    public long lastPacketSN(){
        return lastReceivedSN;
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
        return lastPacketReceivedPayload;
    }





}
