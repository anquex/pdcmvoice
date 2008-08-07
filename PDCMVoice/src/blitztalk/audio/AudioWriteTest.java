package blitztalk.audio;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import org.xiph.speex.spi.SpeexAudioFileWriter;
import org.xiph.speex.spi.SpeexEncoding;
import org.xiph.speex.spi.SpeexFileFormatType;
import org.xiph.speex.spi.SpeexFormatConvertionProvider;

import blitztalk.client.ClientSideClient;

/**
 * CLASS WAS USED FOR TESTING PURPOSES ONLY!
 * Used in conjuction with AudioControlTest.
 * Just like the AudioEncoderThread but writes to file instead of giving to transport to send
 * @author Val
 *
 */
public class AudioWriteTest extends Thread {

	private TargetDataLine		audioDataLine;		//mic input
	private AudioInputStream speexAudioInputStream; //encoder stream
	private boolean send = true;
	//private ClientSideClient transport;  //not used in this test

	public AudioWriteTest() {
		send = true;
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

		audioDataLine.start();
		
		try{

			while(send){

				File outputFile = new File("TestOutputFile.spx");

				try
				{
					System.out.println(SpeexAudioFileWriter.SPEEX_FORMAT[0].toString());
					SpeexAudioFileWriter writer = new SpeexAudioFileWriter();
					writer.write(speexAudioInputStream, SpeexFileFormatType.SPEEX, outputFile);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}        
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
