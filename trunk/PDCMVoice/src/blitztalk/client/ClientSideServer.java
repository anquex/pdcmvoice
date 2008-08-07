package blitztalk.client;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import blitztalk.audio.AudioControl;

/**
 * This class represents the server aspect of a client during a call.  It receives UDP
 * packets from the other client, buffers them for some time period to sort out jitter, 
 * and gives the encoded audio to the decoder at regular time intervals depending on how 
 * much time each audio packet represents.  It is designed to be run as a thread alongside
 * the rest of the code (so that packets can be received at any time regardless of what else
 * may be happening).
 * 
 * Date: 5/26/07
 * 
 * @author David Liechty
 *
 */

public class ClientSideServer implements Runnable {

	// Port at which UDP packets are received
	public static final int CALL_PORT = 4123;
	// Number of packets we want to be buffered at any one time.
	public static final int NUM_PACKETS_BUFFERED = 5;
	// Number of bytes of audio data in each packet
	public static final int BYTES_PER_DATA = 320;
	// Number of bytes actually in packet
	
	public static final int HEADER_LENGTH = 5;
	public static final int BYTES_PER_PACKET = BYTES_PER_DATA + HEADER_LENGTH; // for seq number
	// Number of spaces in the buffer array
	public static final int BUFFER_LENGTH = 30 * NUM_PACKETS_BUFFERED; // In packets
	// Amount of time each packet of audio data represents
	public final int TIME_PER_PACKET = 20; // in ms
	

	// The timer that sends audio data to decoder at a fixed interval
	private BufferTimer timer = null;
	// The thread that uses the timer to run along side the socket code.
	private Thread timerThread = null;

	// The socket used to receive packets
	private DatagramSocket socket = null;

	// The buffer of packets
	protected static DatagramPacket[] buffer = null;

	// The audio decoder
	private AudioControl audio;

	/**
	 * This constructor allows for a customized name for this Thread.  It also initializes
	 * the socket, buffer, and timer for this class.
	 * 
	 * @param name
	 * @throws IOException
	 */
	public ClientSideServer(AudioControl audio) throws IOException {
		socket = new DatagramSocket(CALL_PORT);
		buffer = new DatagramPacket[BUFFER_LENGTH];
		this.audio = audio;
	}

	/**
	 * This method is the loop which receives packets.  It will stop looping when
	 * connectionValid is no longer true.  It basically just passes the received packets
	 * to a parser thread that knows what to do with them.  This method assumes that packets
	 * will not be arriving in quick succession (because data should only be sent every time
	 * a new packet is made), so it makes a new thread for every packet that arrives.
	 */
	public void run() {
		
		while(InitServer.callActive) {

			try {
				byte[] buf = new byte[BYTES_PER_PACKET];

				// receive the packet
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);

				System.out.println("Packet received");


				// Start timer if this is the first packet received, and give it the initial
				// sequence number
				if (timer == null) {
					int firstSeq = packet.getData()[0];
					timer = new BufferTimer(TIME_PER_PACKET, firstSeq, audio);
					timerThread = new Thread(timer, "timerThread");
					timerThread.start();
					System.out.println("Timer started");
				}
				

				// give packet to parser thread
				ParsePacket parser = new ParsePacket(packet);
				(new Thread(parser)).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// close socket when connection is done.
		socket.close();
	}
}

/**
 * This class is the parser thread that takes a packet and prepares it to be passed to the
 * audio decoder.
 * 
 * Date: 5/26/07
 * 
 * @author David Liechty
 *
 */
class ParsePacket implements Runnable {

	// The packet being parsed
	DatagramPacket packet;
	// The buffer of ALL packets currently in buffer
	DatagramPacket[] buffer;
	// the audio decoder
	AudioControl audio;
	
	/**
	 * Initializes variables
	 * 
	 * @param packet packet that needs parsing
	 */
	public ParsePacket(DatagramPacket packet) {
		this.packet = packet;
	}

	/**
	 * Basically this just inserts the packet in the buffer in the proper place.
	 */
	public void run() {
		byte[] data = packet.getData();

		// The sequence 
		int seqNum = (int) data[0];
		
		// add packet to buffer
		synchronized (ClientSideServer.buffer) {
			ClientSideServer.buffer[seqNum % ClientSideServer.BUFFER_LENGTH] = packet;
		}
	}
}

/**
 * This class is the timer that spits out audio data at appropriate times to be
 * decoded and played back for the user.
 * 
 * Date: 5/26/07
 * 
 * @author David Liechty
 *
 */
class BufferTimer implements Runnable {

	// iterator for buffer
	private static int bufIter;

	// delay at which data is given to decoder
	private int delay;

	// array that is the actual audio data
	private byte[] audioData;
	// array that is the audio data plus the sequence number
	private byte[] dataPlusHeader;

	// just a temporary storage packet
	private DatagramPacket tempPacket;
	// the audio decoder
	private AudioControl audio;


	/**
	 * Initializes iterator and the delay between packets
	 * @param delay the delay between released packets.  Also corresponds to the span of time encoded in the audio.
	 */
	public BufferTimer(int delay, int firstSeq, AudioControl audio) {
		this.delay = delay;
		bufIter = firstSeq % ClientSideServer.BUFFER_LENGTH;
		this.audio = audio;
	}

	/**
	 * This method contains the main loop that gives audio data to the decoder.
	 */
	public void run() {

		// Initial Delay to let buffer fill
		try {
			Thread.sleep(delay * ClientSideServer.NUM_PACKETS_BUFFERED);
			//Thread.sleep(100);
		} catch (Exception e) {
			e.printStackTrace();
		}

		while(InitServer.callActive) {


			// gets next packet from buffer
			tempPacket = getNextPacket(delay);

			if (tempPacket != null) {

				byte[] lengthData = new byte[4];

				int length;

				dataPlusHeader = tempPacket.getData();

				for (int i = 1; i < ClientSideServer.HEADER_LENGTH; i++) {
					lengthData[i - 1] = dataPlusHeader[i];
				}

				length = Packer.packInt(lengthData);

				System.out.println("Length of data array: " + length);

				audioData = new byte[ClientSideServer.BYTES_PER_DATA];

				for (int i = 0; i < audioData.length; i++) {
					audioData[i] = dataPlusHeader[i + ClientSideServer.HEADER_LENGTH];
				}

				// pass raw audio data in byte array form to Valery
				passToAudio(audioData, length);

			}

			// iterate iterator into array such that it wraps when it reaches 
			iterate();

			// Only need to delay for a single packet's length from now on
			try {
				Thread.sleep(delay);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * This method retrieves the next packet that needs to be decoded from the buffer
	 * 
	 * @return The next packet that needs to be decoded
	 */
	private synchronized DatagramPacket getNextPacket(int delay) {

		DatagramPacket nextPacket;

		// In case of lost packets, it checks to see if the expected place in the array is null
		// and if it is, it just skips that packet.  The assumption is that if the packet
		// that was supposed to go there arrives later, it will be overwritten and thus dropped
		if (ClientSideServer.buffer[bufIter] == null) {
			//iterate();
			//iter++;
			//nextPacket = getNextPacket(delay);
			nextPacket = null;
		} else {
			nextPacket = ClientSideServer.buffer[bufIter];
			
			// every time a packet is extracted it's removed from the buffer so that the next
			// time a wrap occurs and this method gets back to the same array index, this method
			// won't get confused
			ClientSideServer.buffer[bufIter] = null;
		}

		return nextPacket;
	}

	/**
	 * iterates bufIter such that it wraps when it reaches the size of the buffer
	 *
	 */
	private void iterate() {
		bufIter++;
		bufIter = bufIter % ClientSideServer.BUFFER_LENGTH;
	}

	/**
	 * This method will pass the audio data from this class to the decoder
	 * @param audioData The audio data that needs to be decoded in byte array form.
	 */
	private void passToAudio(byte[] audioData, int length) {
		//String testString = new String(audioData, 0, audioData.length);
		//System.out.println(testString);
		
		//int checkSum = 0;
		
		/*for (int i = 0; i < audioData.length; i++) {
			checkSum = checkSum + audioData[i];
		}*/
		
		System.out.println("Sending packet to decoder.");
		audio.decodeData(audioData, length);
	}
} 