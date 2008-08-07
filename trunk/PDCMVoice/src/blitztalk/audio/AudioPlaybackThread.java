package blitztalk.audio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

/**
 * This class is used for playback of decoded data
 * For testing, there is code left involving unencoded data so that writing to 
 * a file can take place under the same exact conditions that playback would have
 * taken place.  Testing can only be done with modification of the source code as written
 * in the comments.
 * 
 * @author Val
 *
 */
public class AudioPlaybackThread extends Thread {

	/**
	 * Number of bytes player attemps to read (and play) from decoded stream at one time
	 */
	public final static int DECODED_PACKET_SIZE = 4096;


	private AudioInputStream audioInputStream;  //this is decoded

	//When user wants to write to a file instead of playing back direcly (for testing),
	//there are several places in the code where comments must be read carefully and code below
	//those comments either commented out or uncommented.
	private File outputFile; //FOR TESTING ONLY - output file to write to
	private InputStream inputStreamPiped; //FOR TESTING ONLY - still encoded data stream
	private FileOutputStream fw; //FOR TESTING ONLY - used to write to file


	private boolean play; //turns playback on and off

	/**
	 * Use this constructor for general playback purposes.
	 * @param inputStream audio input stream ready for playback (decoded)
	 */
	public AudioPlaybackThread (AudioInputStream inputStream){
		audioInputStream = inputStream;
		play = true;
	}

	/**
	 * ONLY TO BE USED FOR TESTING (Writing to file, instead of playback)
	 * @param inputStream speex2pcm decoded stream
	 * @param piped	predecoded stream, piped directly to encoded data
	 */
	public AudioPlaybackThread (AudioInputStream inputStream, InputStream piped){
		play = true;
		audioInputStream = inputStream;
		inputStreamPiped = piped;
		outputFile = new File("TestOutputFile.spx");
		try{
			fw = new FileOutputStream(outputFile,true);
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}

	public void run(){
		try
		{

			/*
			  From the AudioInputStream, we fetch information about the format of the
			  audio data.
			 */
			AudioFormat	audioFormat = audioInputStream.getFormat();

			/*
			  First, we have to say which kind of line we want. The
			  possibilities are: SourceDataLine (for playback), Clip
			  (for repeated playback)	and TargetDataLine (for
			  recording).
			  Here, we want to do normal playback, so we ask for
			  a SourceDataLine.
			  Then, we have to pass an AudioFormat object, so that
			  the Line knows which format the data passed to it
			  will have.
			 */
			SourceDataLine	line = null;
			DataLine.Info	info = new DataLine.Info(SourceDataLine.class, audioFormat);

			try {
				line = (SourceDataLine) AudioSystem.getLine(info);				
				line.open(audioFormat);
			}
			catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}

			/*
			  The line now can receive data, but will not pass them on to the audio output device
			  (which means to your sound card). This has to be activated.
			 */
			line.start();

			/*
			  We have to write data to the line. We do this  in a loop. First, we read data from 
			  the AudioInputStream to a buffer. Then, we write from this buffer to the Line.
			 */
			int	nBytesRead = 0;
			byte[]	abData = new byte[AudioControl.EXTERNAL_BUFFER_SIZE];
			while (play)
			{
				try
				{
					//COMMENT this line out when you want to WRITE TO FILE
					nBytesRead = audioInputStream.read(abData, 0, DECODED_PACKET_SIZE);


					//UNCOMMENT this block if you want to WRITE TO FILE
					/*try
					{

		            	nBytesRead = inputStreamPiped.read(abData, 0 ,AudioControl.PACKET_SIZE);
		            	System.out.println("About to write to file ");
		            	fw.write(abData,0,nBytesRead);

					}
					catch (Exception e)
					{
						e.printStackTrace();
					} */
				}
				catch (Exception e)
				{
					if (e.getMessage().compareToIgnoreCase("Write end dead")==0){
						//This exception is normally ok, data just not ready
						//System.out.println("Write end dead");
					}
					else{
						e.printStackTrace();
					}
				}

				//COMMENT this if-clause out if you want to WRITE TO FILE
				if (nBytesRead > 0) {
					//write to the audio out line (to the speaker/sound card)
					line.write(abData, 0, nBytesRead);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Turns off the playback
	 */
	public void stopPlayback(){
		this.play = false;
	}

}
