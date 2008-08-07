package blitztalk.audio;

import java.io.File;
import java.io.IOException;
import java.io.PipedOutputStream;

import org.xiph.speex.spi.SpeexAudioFileWriter;
import org.xiph.speex.spi.SpeexFileFormatType;

import blitztalk.client.ClientSideClient;

/**
 * CLASS USED FOR TESTING PURPOSES ONLY!
 * Used to write to file.  No playback involved.
 * Methods for decoding and playing the data as it comes in
 * Additional threads are used to take care of encoding and sending the data.
 * 
 * @author Val
 *
 */
public class AudioControlTest {

	public final static int PACKET_SIZE = 20; //how many bytes per packet
	public final static int EXTERNAL_BUFFER_SIZE = 64000;


	//Extensions of the thread class that take care sending and receiving audio
	private AudioWriteTest encoder; //collects byte[] of size PACKET_SIZE from buffer/stream and sends

	//private AudioPlaybackThread player; //not used in this test
	private PipedOutputStream pipedOutputStream; //decoder writes to this stream

	
	public static void main(String[] args){
		AudioControlTest audioControl = new AudioControlTest();
		audioControl.start();
	}
	
	public void start(){
		encoder = new AudioWriteTest();

		/*player = new AudioPlaybackThread();
		*/

		//start the encoding and sending threads
		encoder.start();

		//start the playback thread
		//player.start();
	}

	/**
	 * NOT USED IN THIS TEST.
	 * DOES NOTHING.
	 * 
	 * @param audioData
	 */
	public void decodeData(byte[] encodedData, int len){

		/*try {
			byte[] data = new byte[len];
			System.arraycopy(encodedData, 0, data, 0, len); //just a precaution
			pipedOutputStream.write(data,0,len); //this pipe feeds directly into the decoder stream
		}
		catch (Exception e){
			e.printStackTrace();
		}
		*/

	}

}
