package pdcmvoice.recovery;

/**
 * 
 * @author Antonio
 */
public class RecoverySample 
{
    public int sn; 
    public byte[] audioPkt; //pacchetto speex
    public boolean marked;
    
    
    public RecoverySample(int sn, byte[] audioPkt, boolean marked)
    {
        this.sn = sn;
        this.audioPkt = audioPkt;
        this.marked = marked;
    }
}
