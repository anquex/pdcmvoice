package blitztalk.audio;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.xiph.speex.spi.Speex2PcmAudioInputStream;
import org.xiph.speex.spi.SpeexEncoding;

/**
 * CLASS USED FOR TESTING PURPOSES ONLY!
 * Used in conjunction with AudioEncoderTest.
 * Methods for decoding and playing the data as it comes in
 * Additional threads are used to take care of encoding and sending the data.
 * 
 * @author Val
 *
 */
public class AudioControlStreamTest {

	public final static int PACKET_SIZE = 20; //how many bytes per packet
	public final static int EXTERNAL_BUFFER_SIZE = 64000;


	//Extensions of the thread class that take care sending and receiving audio
	private AudioEncoderTest encoder; //collects byte[] of size PACKET_SIZE from buffer/stream and sends

	private AudioPlaybackThread player; //continously plays back decoded data from playback buffer
	private PipedOutputStream pipedOutputStream; //decoder writes to this stream
	
	public static void main(String[] args){
		AudioControlStreamTest audioControl = new AudioControlStreamTest();
		audioControl.start();
	}

	public void start(){
		//create the playback and encoding threads
		player = new AudioPlaybackThread(getDecoderStream());
		encoder = new AudioEncoderTest(this);
		
		//start the threads
		player.start();
		encoder.start();
	}

	private AudioInputStream getDecoderStream() {
		AudioFormat speexFormat = new AudioFormat(SpeexEncoding.SPEEX_Q0,
				44100.0F,
				-1,
				2,
				-1,
				-1, // frame rate
				false);

		AudioFormat pcmFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
				speexFormat.getSampleRate(),
				16,
				speexFormat.getChannels(),
				speexFormat.getChannels() * 2,
				speexFormat.getSampleRate(),
				false);
		
		//set up streams for decoder and player threads
		try {
			//By writing to the piped output stream, the decoder will effectively be writing
			//to the speex input stream
			PipedInputStream inputStream = new PipedInputStream(); //used to create the speex2pcm input stream, which player can play
			pipedOutputStream = new PipedOutputStream(inputStream);
			
			Speex2PcmAudioInputStream speex2pcm = new Speex2PcmAudioInputStream(inputStream, pcmFormat, AudioSystem.NOT_SPECIFIED);
			return speex2pcm;
			
		}
		catch (Exception e){
			e.printStackTrace();
			System.exit(1);
		}
		
		return null;
	}

	/**
	 * Takes byte[] of data from transport and decodes, plays it
	 * A new thread is created each time the decoder gets more data
	 * @param audioData
	 */
	public void decodeData(byte[] encodedData, int len){

		try {
			byte[] data = new byte[len];
			System.arraycopy(encodedData, 0, data, 0, len);
			pipedOutputStream.write(data,0,len);

		}
		catch (Exception e){
			e.printStackTrace();
		}

	}

}
