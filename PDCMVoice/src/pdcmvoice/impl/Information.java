/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.impl;

/**
 *
 * @author Laura
 */

import java.io.*;
import pdcmvoice.settings.*;

public class Information extends Object implements  Serializable {
    
      private VoiceSessionSettings voiceSessionSett;
    private TransmissionSettings transmissionSett;
      private AudioSettings audioSett;
      private ConnectionSettings connSett;
    
    public Information(){
        //per al VM in fase di deserializzazione
    }

    public Information(AudioSettings as, ConnectionSettings cs, 
                        /*VoiceSessionSettings vss,*/TransmissionSettings ts){
        audioSett = as;
        connSett = cs;
//        voiceSessionSett = vss;
        transmissionSett = ts; 
    }
  
    private void writeObject(ObjectOutputStream out) throws IOException {
        //scrivo gli oggetti contenti le info di cui ho bisogno 
        out.writeObject(audioSett);        
        out.writeObject(connSett);
//        out.writeObject(voiceSessionSett);
        out.writeObject(transmissionSett);
        System.out.println("Scrittura avvenuta correttamente");
        
  
    }
  private boolean doterminate(ObjectInputStream in) throws IOException {
        if(in.available() >= 0)
          return false;
        return true;
        
    }
  
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
     int formatCode = 0;
       try{
        while(!doterminate(in)){
            //leggo gli oggetti contenuti 
           Object obj = in.readObject();
           if(obj instanceof AudioSettings){
                this.audioSett = (AudioSettings)obj;
               //per test
                formatCode  = this.audioSett.getFormat();
                int speexQuality = this.audioSett.getSpeexQuality();
                System.out.println("formate code: " + formatCode + " \nspeex quality: "+ speexQuality);                            
            }
//            else if(obj instanceof VoiceSessionSettings){
//                this.voiceSessionSett = (VoiceSessionSettings)obj;
//                //porte usate dal sender nella voicesessionsettings per test
//                int RTPport = voiceSessionSett.getLocalRTPPort();
//                int RTCPport = voiceSessionSett.getLocalRTCPPort();
//                int getrecoveryPort = voiceSessionSett.getLocalRecoveryPort();
//                int RTPremotePort = voiceSessionSett.getRemoteRTPPort();
//                int RTCPremotePort = voiceSessionSett.getRemoteRTCPPort();
//                int RemoteRecoveryPort = voiceSessionSett.getRemoteRecoveryPort();
//                //setto i parametri del receiver 
//                //forse da togliere
//                voiceSessionSett.setRemote(formatCode, voiceSessionSett.getRemoteAddress(),
//                                            RTPremotePort,RTCPremotePort, RemoteRecoveryPort);       
//           }
           else if (obj instanceof ConnectionSettings){
                this.connSett = (ConnectionSettings)obj;
                //per test
                
                System.out.println("Master port: " + connSett.getMaster());
                System.out.println("RTCP port: " + connSett.getRTCP());
                System.out.println("RTP port: " + connSett.getRTP());
                System.out.println("Recovery port: " +  connSett.getRecovery());
                
                
            }
            else if(obj instanceof TransmissionSettings){
                this.transmissionSett = (TransmissionSettings)obj;
                //per test
                System.out.println("Numero frame per pacchetto: " + transmissionSett.getFramesPerPacket());
                System.out.println("Max buffer size: " + transmissionSett.getMaxBufferSize());
                System.out.println("Min buffer Size: " + transmissionSett.getMinBufferSize());
                
            }
        }
       }catch(OptionalDataException ex){          
      }
    }
    
     private void readObjectNoData() throws ObjectStreamException{
     }
     
    public AudioSettings getAudioSettings(){
        return audioSett;
    }
    public ConnectionSettings getConnectionSettings(){
        return connSett;
    }
    public VoiceSessionSettings getVoiceSessionSettings(){
        return voiceSessionSett;
    }
    public TransmissionSettings getTransmissionSettings(){
        return transmissionSett;
    }
    
   
}
