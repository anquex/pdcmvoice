package blitztalk.audio;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.xiph.speex.spi.Speex2PcmAudioInputStream;
import org.xiph.speex.spi.SpeexEncoding;

import blitztalk.client.ClientSideClient;

/**
 * Controls the autio encoding, sending, decoding, and playback.
 * Decoding is accesible from this class.
 * Additional threads are used to take care of encoding, sending, and playback of the data.
 * 
 * @author Val
 *
 */
public class AudioControl {

	/**
	 *how many bytes per packet (encoded)
	 */
	public final static int PACKET_SIZE = 20;
	
	/**
	 * Used for reading in decoded data from stream
	 */
	public final static int EXTERNAL_BUFFER_SIZE = 64000;


	//Extensions of the thread class that take care of encoding, sending, and playing audio
	private AudioEncoderThread encoder; //listens to mic and encodes into send buffer/stream
	private AudioPlaybackThread player; //continously plays back decoded data from playback buffer
	
	private PipedOutputStream pipedOutputStream; //decoder writes to this stream

	/**
	 * Begin simultaneous audio encoding, sending, decoding, and playback
	 * @param transport Transport layer responsible for sending audio data
	 */
	public void start(ClientSideClient transport){
		player = new AudioPlaybackThread(getDecoderStream());
		player.start();
		
		encoder = new AudioEncoderThread(transport);
		encoder.start();
	}

	/**
	 * Stops encoding, sending, and playback
	 */
	public void stop(){
		this.encoder.stopEncoding();
		this.player.stopPlayback();	
	}
	
	//Returns the speex2pcm decoder stream
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
		
		//set up streams for decoder and the player thread
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
	 * Takes byte[] of data from transport and gets it ready to be played (writes to decoder stream)
	 * Calling this method is all that's necessary in order to have the data be decoded
	 * and then automatically played back.
	 * @param encodedData data obtained directly from transport (still encoded)
	 * @param len number of bytes of ACTUAL data in the packet
	 */
	public void decodeData(byte[] encodedData, int len){

		try {
			byte[] data = new byte[len];
			System.arraycopy(encodedData, 0, data, 0, len); //just a precaution
			pipedOutputStream.write(data,0,len); //this pipe feeds directly into the decoder stream
		}
		catch (IOException io){
			if (io.getMessage().compareToIgnoreCase("Pipe broken")==0){
				//This exception is normally ok, connection must be closed
			}
			else{
				io.printStackTrace();
			}
		}

	}

}