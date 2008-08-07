package blitztalk.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

import org.xiph.speex.spi.Speex2PcmAudioInputStream;
import org.xiph.speex.spi.SpeexEncoding;

/**
 * CLASS USED FOR TESTING PURPOSES ONLY!
 * Decodes spx file and plays it.
 * Methods for decoding and playing the data as it comes in
 * Additional threads are used to take care of encoding and sending the data.
 * 
 * @author Val
 *
 */
public class AudioControlPlaybackTest {

	//public final static int PACKET_SIZE = 320; //how many bytes per packet
	public final static int EXTERNAL_BUFFER_SIZE = 64000;


	//Extensions of the thread class that take care sending and receiving audio
	//private AudioEncoderThread encoder; //listens to mic and encodes into send buffer/stream
	//private AudioSenderThread sender; //collects byte[] of size PACKET_SIZE from buffer/stream and sends

	private AudioPlaybackThread player; //continously plays back decoded data from playback buffer
	private PipedOutputStream pipedOutputStream; //decoder writes to this stream

	public static void main(String[] args){
		try{
			AudioControlPlaybackTest audioControl = new AudioControlPlaybackTest();
			File speexFile = new File("TestOutputFile.spx");

			audioControl.start();

			InputStream input = new FileInputStream(speexFile);
			int nBufferSize = 320;
			byte[]	abBuffer = new byte[nBufferSize];
			while (true)
			{
				System.out.println("trying to read (bytes): " + abBuffer.length);
				int	nBytesRead = input.read(abBuffer,0,nBufferSize);
				System.out.println("read (bytes): " + nBytesRead);
				if (nBytesRead > 0) {
					byte[] data = new byte[nBytesRead];
					System.arraycopy(abBuffer, 0, data, 0, nBytesRead);
					audioControl.decodeData(data, nBytesRead);
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void start(){
		//encoder = new AudioEncoderThread();
		//sender = new AudioSenderThread(transport, encoder.getAudioInputStream());

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
			player = new AudioPlaybackThread(speex2pcm);
		}
		catch (Exception e){
			e.printStackTrace();
			System.exit(1);
		}

		//start the encoding and sending threads
		//encoder.start();

		//start the playback thread
		player.start();
	}

	/**
	 * Takes byte[] of data from transport and decodes, plays it
	 * A new thread is created each time the decoder gets more data
	 * @param audioData
	 */
	public void decodeData(byte[] encodedData, int len){

		try {
			byte[] data = new byte[len];
			System.arraycopy(encodedData, 0, data, 0, len); //just a precaution
			pipedOutputStream.write(data,0,len); //this pipe feeds directly into the decoder stream

		}
		catch (Exception e){
			e.printStackTrace();
		}

	}

}
