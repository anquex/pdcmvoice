/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.settings;

import java.io.Serializable;
import static pdcmvoice.impl.Constants.*;

/**
 *
 * @author marco
 */
public class ConnectionSettings implements Serializable{


    // ports
    private int master;
    private int rtp;
    private int rtcp;
    private int recovery;

    public ConnectionSettings(){
        this(DEFAULT_MASTER_PORT,
            DEFAULT_RTP_PORT,
            DEFAULT_RTCP_PORT,
            DEFAULT_RECOVERY_PORT);
    }

    public ConnectionSettings(int master,int rtp, int rtcp, int recovery){
        this.master=master;
        this.rtp=rtp;
        this.rtcp=rtcp;
        this.recovery=recovery;
    }

    public int getRTP(){
        return rtp;
    }
    public int getMaster(){
        return master;
    }
    public int getRTCP(){
        return rtcp;
    }
    public int getRecovery(){
        return recovery;
    }
    public void setRTP(int rtp){
        this.rtp=rtp;
    }

    public void setMaster(int m){
        master=m;
    }
    public void setRTCP(int r){
        rtcp=r;
    }
    public void setRecovery(int r){
        recovery=r;
    }




    public void setRTP(String s){
        int n=DEFAULT_RTP_PORT;
        try{
        int temp=Integer.parseInt(s);
        if (temp>1023) n=temp;
        }catch(NumberFormatException e){

        }
        this.rtp=n;
    }

    public void setMaster(String s){
        int n=DEFAULT_MASTER_PORT;
        try{
        int temp=Integer.parseInt(s);
        if (temp>1023) n=temp;
        }catch(NumberFormatException e){
        }
        master=n;
    }
    public void setRTCP(String s){
        int n=DEFAULT_RTCP_PORT;
        try{
        int temp=Integer.parseInt(s);
        if (temp>1023) n=temp;
        }catch(NumberFormatException e){

        }
        rtcp=n;
    }
    public void setRecovery(String s){
        int n=DEFAULT_RECOVERY_PORT;
        try{
        int temp=Integer.parseInt(s);
        if (temp>1023) n=temp;
        }catch(NumberFormatException e){
        }
        recovery=n;
    }
}

