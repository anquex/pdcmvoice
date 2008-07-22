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
    private long lastReceivedSN=0;
    private boolean inited;

    private RecoveryCollection remote;

    PlayoutBuffer playoutBuffer;

    //DEBUG
    private byte[] lastNewVoice;

//    int avg=0;
//    int packets=0;
//    long LastTimeStamp=0;
//    long currentTimeStamp=0;

    public Depacketizer(RTPSession s){
        rtpSession=s;
        // disable rtp buffering, recive all packets!
        rtpSession.packetBufferBehavior(0);


    }

    public Depacketizer(RTPSession s, RecoveryCollection remote){
        this(s);
        rtpSession=s;
    }

    public void receiveData(DataFrame frame, Participant participant)
    {
        if (!inited) return;

        /*  ------------------------------
         *  --- SEND TO PLAYOUT BUFFER ---
         *  ------------------------------ */

        byte[] voice=frame.getConcatenatedData();
        int lenght=0;

        if (DEBUG){
            String out="";
            out+="Received Packet with";
            out+=" PAYLOAD: "+ rtpSession.payloadType();
            out+=" AUDIO :"+ voice.length;
            out+=" MARKED :"+ frame.marked();
            out+=" TIMESTAMP :"+ frame.rtpTimestamp();
            out+=" SN :"+ frame.sequenceNumbers()[0];
            out(out);
        }

        /* ---------------------------------------
         * --- RECOVERY COLLECTION CODE BEGINS ---
         * ---------------------------------------*/

        // collection.add(frame.sequenceNumbers()[0], voice, frame.rtpTimestamp());

        byte[] toSend = new byte[remote.getPktSize()];
        System.arraycopy(voice, 0, toSend, 0, remote.getPktSize()); //singolo pacchetto voce: 20Byte
        //ATTENZIONE!!!
        //SIMULAZIONE PERDITA PACCHETTI
        if ((int)frame.sequenceNumbers()[0] % 10 != 0)//SIMULAZIONE PERDITA PACCHETTI
        this.remote.add((int)frame.sequenceNumbers()[0], toSend, frame.rtpTimestamp());

        if (frame.marked())
        {
            System.arraycopy(voice, remote.getPktSize(), toSend, 0, remote.getPktSize());
            //ATTENZIONE!!!
            //SIMULAZIONE PERDITA PACCHETTI
            if ((int)frame.sequenceNumbers()[1] % 10 != 0)//SIMULAZIONE PERDITA PACCHETTI
            this.remote.add((int)frame.sequenceNumbers()[1], toSend, frame.rtpTimestamp());
        }

        /* ---------------------------------------
         * --- RECOVERY COLLECTION CODE ENDS -----
         * ---------------------------------------*/

        if (isRDT(frame.payloadType()) || frame.marks()[0]){
            lenght=voice.length/2;
            byte[] v=new byte[voice.length/2];
            System.arraycopy(voice, 0, v, 0, lenght);
            playoutBuffer.add(frame.rtpTimestamp(), v);
            System.arraycopy(voice, lenght, v, 0, lenght);
            playoutBuffer.add(frame.rtpTimestamp()-20, v);
        }
        else{
            playoutBuffer.add(frame.rtpTimestamp(), voice);
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
                playoutBuffer=new PlayoutBuffer(d);
                return true;
        }
    }

    public void userEvent(int type, Participant[] participant) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public int frameSize(int payloadType) {
        // 1 packet -> at least 1 frame
        return 1;
    }





}
