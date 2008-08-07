package blitztalk.client;

import java.io.UnsupportedEncodingException;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;

/** 
 * Handle DES encryption for password verification
 * @author tcarney
 *
 */
public class DESEncryption {
	private static final int PASS_LENGTH = 8;
	
	public static void main(String[] args) {
		
		stringToKey("abcdefg");
	}
	
	/**
	 * Takes string password and returns string to send to DND server
	 * @param password User's password
	 * @param random Random number from server
	 * @return random encrypted using password as an octal string
	 */
	public static String encrypt(String password, String random) {

		// Convert password to an 8byte array
		byte[] pass = stringToKey(password);
		
		try {
			KeySpec ks = new DESKeySpec(pass);
	        SecretKeyFactory kf = SecretKeyFactory.getInstance("DES");
	        SecretKey ky = kf.generateSecret(ks);
	        Cipher cf = Cipher.getInstance("DES/ECB/NoPadding");
	        cf.init(Cipher.ENCRYPT_MODE,ky);
	        byte[] cph = cf.doFinal(octalToBytes(random));
	        
	        return bytesToOctal(cph);
	        
		} catch (Exception e) {
			System.err.println("DES: Bad input key");
			e.printStackTrace();
			return null;
		}
	}
	
	private static byte[] stringToKey(String input) {
		// Convert string to byte array
		try {
			byte[] inputBytes = input.getBytes("US-ASCII");
			
			// Pad input to set length, by padding with 0
			byte[] output = new byte[PASS_LENGTH];
			
			int j = 0;
			for (int i = 0; i < output.length; i++) {
				
				// Copy input, pad right with 0
				if (j < inputBytes.length) {
					output[i] = inputBytes[j];
					
					// Get parity
					int sum = 0;
					for (int b = 1; b < 7; b++) {
						if ((inputBytes[j]&(1<<b)) > 0)
							sum += 1;
					}
						
					if (sum % 2 == 0)
						output[i] |= 1;
					else
						output[i] &= (byte) 254;
					
					//System.out.println("input byte: " + Integer.toString(inputBytes[j]  & 0xff, 2));
					//System.out.println("sum: " + sum);
					//System.out.println("output byte: " + Integer.toString(output[i]  & 0xff, 2));
				} else {
					output[i] = 0;
				}

				j++;
			}
			
			return output;
			
		} catch (UnsupportedEncodingException e) {
			System.err.println("*** should never get here, encoding error");
			return null;
		}
	}
	
	private static byte[] octalToBytes(String in) {
		byte[] output = new byte[8];
		
		for (int i = 0; i < output.length; i++) {
			output[i] = (byte) Integer.parseInt(in.substring(3*i, 3*i+3), 8);
		}
		
		return output;
	}
	
	private static String bytesToOctal(byte[] in) {
		String out = "";
		String oneByte;
		for (int i = 0; i < in.length; i++) {
			oneByte = Integer.toString(in[i] & 0xff, 8);
			while (oneByte.length() < 3)
				oneByte = "0" + oneByte;
			
			out += oneByte;
		}
		
		return out;
	}
}
