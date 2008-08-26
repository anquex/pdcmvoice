package pdcmvoice.recovery;

/**
 * 
 * @author Antonio
 */
public class RecoverySample 
{
    int sn; 
    byte[] audioPkt; //pacchetto speex
    boolean marked;
    
    
    public RecoverySample(int sn, byte[] audioPkt, boolean marked)
    {
        this.sn = sn;
        this.audioPkt = audioPkt;
        this.marked = marked;
    }
}
