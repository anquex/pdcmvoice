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
public class AudioSettings implements Serializable{

    private int formatCode;
    private int speexQuality;

    public AudioSettings(){
        restoreDefaults();
    }

    public AudioSettings(int f, int q){
        restoreDefaults();
        formatCode=f;
        speexQuality=q;
    }

    public void setFormat(int f){
        formatCode=f;
    }

    public int getFormat(){
        return  formatCode;
    }

    public void setSpeexQuality(int n){
        speexQuality=n;
    }

    public int getSpeexQuality(){
        return speexQuality;
    }

    public void restoreDefaults(){
        formatCode=DEFAULT_FORMAT_CODE;
        speexQuality=SPEEX_QUALITIES[DEFAULT_SPEEX_QUALITY_INDEX];
    }

}
