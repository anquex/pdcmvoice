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



public class AudioEncoderThread
extends Thread
{
	private TargetDataLine		audioDataLine;		//mic input
	private AudioInputStream speexAudioInputStream; //encoder stream
	private boolean send;
	private ClientSideClient transport;


	/**
	 * Initiates necessary stream, but doesn't start up the mic yet
	 *
	 */
	public AudioEncoderThread(ClientSideClient transport)
	{
		this.transport = transport;
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
			out(""+audioDataLine.isActive());
			if (!audioDataLine.isOpen()){
				audioDataLine.open(pcmFormat);
			}
		}
		catch (LineUnavailableException e)
		{
			out("unable to get a recording line");
			e.printStackTrace();
			System.exit(1);
		}


		//Set up audio input stream (will get data from audio data line)
		AudioInputStream audioInputStream = new AudioInputStream(audioDataLine);
		SpeexFormatConvertionProvider converter = new SpeexFormatConvertionProvider();
		speexAudioInputStream = converter.getAudioInputStream(SpeexEncoding.SPEEX_Q0, audioInputStream);
		//speexAudioInputStream = new Pcm2SpeexAudioInputStream(1,Pcm2SpeexAudioInputStream.DEFAULT_QUALITY,audioInputStream, speexFormat, AudioSystem.NOT_SPECIFIED);
	}


	/** Automatically called when the thread is started, starts up the mic and starts sending data
	 */
	public void run()
	{
		/* Starting the TargetDataLine. It tells the line that we now want to read data from it. 
		 * 
		 */
		if (!audioDataLine.isRunning()){
			audioDataLine.start();
		}

		try {
			while(send){
				byte[] frame=new byte[AudioControl.PACKET_SIZE]; 
				int n=speexAudioInputStream.read(frame, 0, frame.length);
				if (n>0){
					transport.send(frame, n); //sends frame of exactly the same size each time
				}
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

	private static void out(String strMessage)
	{
		System.out.println(strMessage);
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

