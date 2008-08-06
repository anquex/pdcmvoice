/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.settings;

import java.io.Serializable;
import jlibrtp.Participant;
import static pdcmvoice.impl.Constants.*;
import jlibrtp.*;

/**
 *
 * @author marco
 */
public class VoiceSessionSettings implements Serializable{

        public  static final boolean DEBUG=true;
        
        private int sendFormatCode;
        private int receiveFormatCode;
        
        private String remoteAddr;
        private int  remoteRTPPort;
        private int  remoteRTCPPort;
        private int  remoteRecoveryPort;
        private int  remoteSpeexQuality;
        
        private int  localRTPPort;
        private int  localRTCPPort;
        private int  localRecoveryPort;
        private int  localSpeexQuality;

        private boolean localDynamicAdaptation;
        private boolean localEnabledRDT;
        private int localFramesPerPacket;
        private boolean localEnabledRecovery;
        private int localMaxBuferSizeMS;
        private int localMinBuferSizeMS;
        

        public VoiceSessionSettings(AudioSettings la,
                                    ConnectionSettings lc,
                                    TransmissionSettings lt,
                                    AudioSettings ra,
                                    ConnectionSettings rc,
                                    String a
                                    )
        {
            sendFormatCode=la.getFormat();
            // localquality ??
            receiveFormatCode=ra.getFormat();
            remoteAddr=a;

            remoteRTPPort  = rc.getRTP();
            remoteRTCPPort= rc.getRTCP();
            remoteRecoveryPort=rc.getRecovery();
            remoteSpeexQuality=ra.getSpeexQuality();

            localRTPPort  = lc.getRTP();
            localRTCPPort = lc.getRTCP();
            localRecoveryPort=lc.getRecovery();
            localSpeexQuality=la.getSpeexQuality();

            localDynamicAdaptation=lt.getDynamic();
            localEnabledRDT=lt.getRDT();
            localEnabledRecovery=lt.getRecovery();
            localFramesPerPacket=lt.getFramesPerPacket();
            localMaxBuferSizeMS=lt.getMaxBufferSize();
            localMinBuferSizeMS=lt.getMinBufferSize();




            if (DEBUG){
                 String out="";
                 out+="-------- VOICE SESSION SETTINGS --------\n";
                 out+="-------- Remote host: "+getRemoteAddress()+":"+getRemoteRTPPort()+":"+getRemoteRTCPPort()+"\n";
                 out+="-------- Local  Settings: RTP "+getLocalRTPPort()+" RTCP "+getLocalRTCPPort();
                 out(out);
            }
        }
        public VoiceSessionSettings(int sendFormat, int receiveFormat, String d){
            sendFormatCode=sendFormat;
            receiveFormatCode=receiveFormat;
            remoteAddr=d;

            // OTHER SETTINGS TO DEFAULT
            remoteRTPPort  = DEFAULT_RTP_PORT;
            remoteRTCPPort=DEFAULT_RTCP_PORT;
            remoteRecoveryPort=DEFAULT_RECOVERY_PORT_LOCAL;
            remoteSpeexQuality=SPEEX_QUALITIES[DEFAULT_SPEEX_QUALITY_INDEX];

            
            localRTPPort  = DEFAULT_RTP_PORT;
            localRTCPPort=DEFAULT_RTCP_PORT;
            localRecoveryPort=DEFAULT_RECOVERY_PORT_LOCAL;
            localSpeexQuality=SPEEX_QUALITIES[DEFAULT_SPEEX_QUALITY_INDEX];

        }
        
        public int getSendFormatCode(){
            return sendFormatCode;
        }
        
        public int getReceiveFormatCode(){
            return receiveFormatCode;
        }
        
        public String getRemoteAddress(){
            return remoteAddr;
        }
        
        public int getLocalRTPPort(){
            return localRTPPort;
        }
        
        public int getLocalRTCPPort(){
            return localRTCPPort;
        }
        
        public int getLocalRecoveryPort(){
            return localRecoveryPort;
        }
        
         public int getRemoteRTPPort(){
            return remoteRTPPort;
        }
        public int getRemoteRTCPPort(){
            return remoteRTCPPort;
        }
        public int getRemoteRecoveryPort(){
            return remoteRecoveryPort;
        }
        
        /**
         * 
         * @return remote RTP/RTCP partecipant
         */
        public Participant getPartecipant(){
            if(remoteAddr==null) return null;
            return new Participant(remoteAddr, remoteRTPPort, remoteRTCPPort);
        }

        public void setRemote(int f ,String addr,int rtp,int rtcp, int rec){
            remoteAddr=addr;
            if(f!=0) receiveFormatCode=f;
            if (rtp>0) remoteRTPPort=rtp;
            if (rtcp>0) remoteRTCPPort=rtcp;
            if (rec>0) remoteRecoveryPort=rec;
        }

        public void setLocal(int f ,int rtp,int rtcp, int rec){
            if(f!=0) sendFormatCode=f;
            if (rtp>0) localRTPPort=rtp;
            if (rtcp>0) localRTCPPort=rtcp;
            if (rec>0) localRecoveryPort=rec;
        }

        public void setRemoteAddress(String addr){
            this.remoteAddr=addr;
        }

        public int getLocalSpeexQuality(){
            return localSpeexQuality;
        }

        public void setLocalSpeexQuality(int i){
            localSpeexQuality=i;
        }
        public boolean isDynamic(){
            return localDynamicAdaptation;
        }

        public boolean isRDT(){
            return localEnabledRDT;
        }
        public boolean withRecovery(){
            return localEnabledRecovery;
        }
        public int framesPerPacket(){
            return localFramesPerPacket;
        }
        public int getMaxBufferSize(){
            return localMaxBuferSizeMS;
        }
        public int getMinBufferSize(){
            return localMinBuferSizeMS;
        }
}
