package blitztalk.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import org.xiph.speex.spi.SpeexEncoding;
import org.xiph.speex.spi.SpeexFormatConvertionProvider;

import blitztalk.client.ClientSideClient;

/**
 * USED FOR TESTING ONLY!
 * Used in conjuction with AudioControlStreamTest
 * @author Val
 *
 */
public class AudioEncoderTest extends Thread {

	private TargetDataLine		audioDataLine;		//mic input
	private AudioInputStream speexAudioInputStream; //encoder stream
	private boolean send = true;
	//private ClientSideClient transport;  //not used in this test
	private AudioControlStreamTest control; //used to decode directly instead of sending across tranport


	public AudioEncoderTest(AudioControlStreamTest control) {
		send = true;
		this.control = control;
		this.getDataLines();
	}


	private void getDataLines() {

		/* For simplicity, the audio data format used for recording
		   is hardcoded here. We use PCM 44.1 kHz, 16 bit signed,
		   stereo.
		 */
		AudioFormat	pcmFormat = new AudioFormat(
				AudioFormat.Encoding.PCM_SIGNED,
				44100.0F, 16, 2, 4, 44100.0F, false);


		/* Now, we are trying to get a TargetDataLine. The
		   TargetDataLine is used later to read audio data from it.
		   If requesting the line was successful, we are opening
		   it (important!).
		 */
		DataLine.Info	info = new DataLine.Info(TargetDataLine.class, pcmFormat);
		audioDataLine = null;

		try
		{
			audioDataLine = (TargetDataLine) AudioSystem.getLine(info);
			audioDataLine.open(pcmFormat);
		}
		catch (LineUnavailableException e)
		{
			System.out.println("unable to get a recording line");
			e.printStackTrace();
			System.exit(1);
		}


		//Set up audio input stream (will get data from audio data line)
		AudioInputStream audioInputStream = new AudioInputStream(audioDataLine);
		SpeexFormatConvertionProvider converter = new SpeexFormatConvertionProvider();
		speexAudioInputStream = converter.getAudioInputStream(SpeexEncoding.SPEEX_Q0, audioInputStream);
		//speexAudioInputStream = new Pcm2SpeexAudioInputStream(1,Pcm2SpeexAudioInputStream.DEFAULT_QUALITY,audioInputStream, speexFormat, AudioSystem.NOT_SPECIFIED);
	}

	public void run(){   

		/* Starting the TargetDataLine. It tells the line that we now want to read data from it. 
		 * 
		 */
		audioDataLine.start();

		try{

			while(send){
				byte[] frame=new byte[AudioControl.PACKET_SIZE]; 
				int n=speexAudioInputStream.read(frame, 0, frame.length);
				System.out.println("Sender thread: "+n+" bytes read from encoder stream");
				//transport.send(frame);
				if (n > 0)
					control.decodeData(frame,n);
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

	/** Stops the encoding and sending.

    Note that stopping the thread explicitely is not necessary. Once
    no more data can be read from the TargetDataLine, no more data can
    be read from our AudioInputStream.
	 */
	public void stopEncoding(){
		this.send = false;
		if (audioDataLine != null){
			audioDataLine.stop();
			audioDataLine.flush();
			audioDataLine.close();
			audioDataLine = null;
		}
	}

}
