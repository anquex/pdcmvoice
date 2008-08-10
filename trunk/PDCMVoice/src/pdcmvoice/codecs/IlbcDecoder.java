/*
 * Experimental implementation of iLBC decoder
 * 
 * Actually can manage only single frames so provide only 
 * signle frame!
 */

package pdcmvoice.codecs;

import net.java.sip.communicator.impl.media.codec.audio.ilbc.ilbc_decoder;
import net.java.sip.communicator.impl.media.codec.audio.Utils;
import static net.java.sip.communicator.impl.media.codec.audio.ilbc.ilbc_constants.*;

/**
 *
 * @author marco
 */
public class IlbcDecoder {
    
    private ilbc_decoder decoder;
    private boolean little;
    private int EncodedShortBlockLeght;
    private short[] decodedData;
    private short[] lastencoded;
    
    public IlbcDecoder(int mode,boolean little)
  {
        decoder=new ilbc_decoder(mode, 0);
        this.little=little;
        if(mode == 20)
            EncodedShortBlockLeght = BLOCKL_20MS;
        else if(mode == 30)
            EncodedShortBlockLeght = BLOCKL_30MS;
        else throw new IllegalArgumentException("Unsupported mode");
        this.little=little;

  }
    
  public int getProcessedData(final byte[] data, final int offset){
      int length=decodedData.length*2;
      byte[] outData= new byte[length];
      Utils.shortArrToByteArr(decodedData, outData, true);
      System.arraycopy(outData, 0, data, offset, length);
      return length;
  } 
  
  public void processData(final byte[] data,
                          final int offset,
                          final int len)
  { 
//      System.out.println(data.length);
      short[] d = Utils.byteToShortArray(data, offset, len, false);
//      System.out.println(d.length);
      decodedData = new short[EncodedShortBlockLeght];
//      System.out.println(decodedData.length);
      int  decodedShorts=decoder.decode(decodedData, // uncompressed Audio Frame
                                 d,           // compressed audio frame
                                 (short) 1);  // 0: bad packet, PLC,
                                              // 1: normal 
      lastencoded=d;
  }
  
  public void processData(final boolean lost){
            int  decodedShorts=decoder.decode(decodedData, // uncompressed Audio Frame
                                 lastencoded,           // compressed audio frame
                                 (short) 0);  // 0: bad packet, PLC,
                                              // 1: normal 
  
  }
  
  public int getProcessedDataByteSize() {
    return decodedData.length*2;
  }
}
