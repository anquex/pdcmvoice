/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.settings;

import java.io.Serializable;
import jlibrtp.Participant;
import static pdcmvoice.impl.Constants.*;
import pdcmvoice.settings.*;

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
        
        private int  localRTPPort;
        private int  localRTCPPort;
        private int  localRecoveryPort;
        

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

            remoteRTPPort  = rc.getRTP();
            remoteRTCPPort= rc.getRTCP();
            remoteRecoveryPort=rc.getRecovery();

            localRTPPort  = lc.getRTP();
            localRTCPPort = lc.getRTCP();
            localRecoveryPort=lc.getRecovery();

            remoteAddr=a;
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
            remoteRTPPort  = DEFAULT_RTP_PORT;
            remoteRTCPPort=DEFAULT_RTCP_PORT;
            remoteRecoveryPort=DEFAULT_RECOVERY_PORT_LOCAL;
            
            localRTPPort  = DEFAULT_RTP_PORT;
            localRTCPPort=DEFAULT_RTCP_PORT;
            localRecoveryPort=DEFAULT_RECOVERY_PORT_LOCAL;
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
      
}
