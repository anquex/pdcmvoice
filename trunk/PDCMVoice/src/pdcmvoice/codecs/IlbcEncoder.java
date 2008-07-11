/*
 * Experimental implementation of iLBC encoder
 * 
 * Actually can manage only single frames so provide only 
 * signle frame!
 */

package pdcmvoice.codecs;

import net.java.sip.communicator.impl.media.codec.audio.ilbc.ilbc_encoder;
import static net.java.sip.communicator.impl.media.codec.audio.ilbc.ilbc_constants.*;
import  net.java.sip.communicator.impl.media.codec.audio.Utils;

/**
 *
 * @author marco
 */
public class IlbcEncoder {
    
    private ilbc_encoder encoder = null;
    private int ILBC_NO_OF_BYTES = 0;
    private boolean little=true; //little endian?
    private short[] result;
    private int no_encoded=0;

    public IlbcEncoder(int mode, boolean little) {
        
        encoder = new ilbc_encoder(mode);
        if(mode == 20)
            ILBC_NO_OF_BYTES = NO_OF_BYTES_20MS;
        else if(mode == 30)
            ILBC_NO_OF_BYTES = NO_OF_BYTES_30MS;
        this.little=little;
        result= new short[ILBC_NO_OF_BYTES/2];
    }
    
    public void processData(byte[] data, int offset, int lenght ){
        short[] d=Utils.byteToShortArray(data, offset, lenght, true);
        no_encoded=encoder.encode(result,d);
    }
    
      /**
   * Pull the decoded data out into a byte array at the given offset
   * and returns the number of bytes of encoded data just read.
   * @param data
   * @param offset
   * @return the number of bytes of encoded data just read.
   */
    
  public int getProcessedData(final byte[] data, final int offset)
  {
    byte[] b=new byte[result.length*2];
    Utils.shortArrToByteArr(result, b, false);
    System.arraycopy(b, 0, data, offset, no_encoded);
    return no_encoded;
  }
    /**
   * Returns the number of bytes of encoded data ready to be read.
   * @return the number of bytes of encoded data ready to be read.
   */
  public int getProcessedDataByteSize()
  {
    return no_encoded;
  }
    
    
    

}
