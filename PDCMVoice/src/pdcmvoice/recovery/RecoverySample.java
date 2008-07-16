package pdcmvoice.recovery;

/**
 * 
 * @author Antonio
 */
public class RecoverySample 
{
    int sn; 
    byte[] audioPkt; //pacchetto speex
    
    
    public RecoverySample(int sn, byte[] audioPkt)
    {
        this.sn = sn;
        this.audioPkt = audioPkt;
    }
}
