/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.settings;

import jlibrtp.Participant;
import static pdcmvoice.impl.Constants.*;

/**
 *
 * @author marco
 */
public class VoiceSessionSettings {
        
        private int sendFormatCode;
        private int receiveFormatCode;
        
        private String remoteAddr;
        private int  remoteRTPPort;
        private int  remoteRTCPPort;
        private int  remoteRecoveryPort;
        
        private int  localRTPPort;
        private int  localRTCPPort;
        private int  localRecoveryPort;
        
    
        public VoiceSessionSettings(int sendFormat, int receiveFormat, String d){
            sendFormatCode=sendFormat;
            receiveFormatCode=receiveFormat;
            
            remoteAddr=d;
            remoteRTPPort  = DEFAULT_RTP_PORT;
            remoteRTCPPort=DEFAULT_RTCP_PORT;
            remoteRecoveryPort=DEFAULT_RECOVERY_PORT;
            
            localRTPPort  = DEFAULT_RTP_PORT;
            localRTCPPort=DEFAULT_RTCP_PORT;
            localRecoveryPort=DEFAULT_RECOVERY_PORT;
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
            
            return new Participant(remoteAddr, remoteRTPPort, remoteRTCPPort);
        }
      
}
