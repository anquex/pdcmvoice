package blitztalk.client;

/**
 * Converts between int and 4-byte arrays.
 * Thanks to java, endianness doesn't matter.
 * 
 * @author tcarney
 */
public class Packer {
	/**
	 * Converts from int to byte array
	 * @param v Integer value
	 * @return 4-byte array
	 */
    public static byte[] unpackInt(int v) {
		byte[] out = new byte[4];
	
		for(int ix = 0; ix < 4; ix++) {
		    out[3 - ix] = (byte)(v & 0xFF);
		    v >>>= 8;
		}

		return out;
    }

    /**
     * Converts from byte array to int
     * @param b 4-byte array
     * @return Integer value
     */
    public static int packInt(byte[] b) {
		int out = 0;
	
		for(int ix = 0; ix < b.length; ix++) {
		    out <<= 8;
	
		    int ival = (int) b[ix];
	
		    if(ival < 0)
			ival += 256;
	
		    out |= ival;
		}

		return out;
    }

    // Test method
    public static void main(String[] args)
    {
	if(args.length == 0) {
	    System.err.println("Usage: Packer <integer>");
	    System.exit(1);
	}

	int  v = Integer.parseInt(args[0]);

	System.out.println("Input value: " + v);
	
	byte[] b = unpackInt(v);

	for(int ix = 0; ix < b.length; ++ix)
	    System.out.println("b[" + ix + "] = " + 
			       Integer.toString((int)b[ix], 16));
	
	int w = packInt(b);

	System.out.println("Final value: " + w);
    }
}
