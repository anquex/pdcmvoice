/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.settings;

import static pdcmvoice.impl.Constants.*;

/**
 *
 * @author marco
 */
public class TransmissionSettings {

    private boolean enabledDynamic=true;
    private int minBufferSize;
    private int maxBufferSize;
    private int framesPerPacket;
    private boolean RDTenable;
    private boolean recoveryEnable;

    public TransmissionSettings(){
        restoreDefaults();
    }

    public void setDynamic(boolean b){
        enabledDynamic=b;
    }
    public boolean getDynamic(){
        return enabledDynamic;
    }

    public void setMinBufferSize(int n){
        if (n>0)
            minBufferSize=n;
        if(getMaxBufferSize()<=getMinBufferSize())
            maxBufferSize=getMinBufferSize()+20;
    }
    public int getMinBufferSize(){
        return minBufferSize;
    }

    public void setMaxBufferSize(int n){
        if (n>0 || n>getMinBufferSize())
            maxBufferSize=n;
    }

    public int getMaxBufferSize(){
        return maxBufferSize;
    }

    public void setFramesPerPacket(int n){
        framesPerPacket=n;
    }
    public void setFramesPerPacket(String s){
        int n=getFramesPerPacket();
        try{
            n=Integer.parseInt(s);
        }
        catch(NumberFormatException e){}
        setFramesPerPacket(n);
    }

    public int getFramesPerPacket(){
        return framesPerPacket;
    }

    public void setRDT(boolean enabled){
        RDTenable=true;
    }
    public boolean getRDT(){
        return RDTenable;
    }

    public void setRecovery(boolean b){
        recoveryEnable=b;
    }

    public boolean getRecovery(){
        return recoveryEnable;
    }

    public void setMinBufferSize(String s){
        int n=getMinBufferSize();
        try{
            n=Integer.parseInt(s);
        }
        catch(NumberFormatException e){};
        setMinBufferSize(n);
    }
    public void setMaxBufferSize(String s){
        int n=getMaxBufferSize();
        try{
            n=Integer.parseInt(s);
        }
        catch(NumberFormatException e){};
        setMaxBufferSize(n);

    }

    public void restoreDefaults(){
        setDynamic(DEFAULT_DYNAMIC_ADAPTATION);
        setFramesPerPacket(ALLOWED_FRAMES_PER_PACKET[DEFAULT_FRAMES_PER_PACKET_INDEX]);
        setMaxBufferSize(DEFAULT_MAX_BUFFER_SIZE);
        setMinBufferSize(DEFAULT_MIN_BUFFER_SIZE);
        setRDT(DEFAULT_RDT_ENABLED);
        setRecovery(DEFAULT_BACKGROUND_RECOVERY);
    }
}
