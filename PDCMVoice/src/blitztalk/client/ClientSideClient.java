package blitztalk.client;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;


/**
 * This class represents the client aspect of a call between clients.  In other words,
 * this class receives the audio data in the form of a byte array and sends it to the
 * server aspect of the other client.
 * 
 * Date: 5/26/07
 * 
 * @author David Liechty
 *
 */


public class ClientSideClient  {

	// port at which client broadcasts
	public static final int CLIENT_PORT = 4124;
	// address of other client
	private InetAddress targetAddress;
	// UDP socket
	private DatagramSocket socket = null;	
	// sequence number for packet (used in buffer on other side)
	private int nextSeq;
	// random number generator for sequence number
	private Random random;
	// array of bytes that includes audio data and sequence number
	private byte[] sendData;
	// The BTClient that created this ClientSideClient
	private BTClient parent;

	/**
	 * This constructor basically just initializes variables, and in particular, chooses
	 * the first sequence number.
	 * 
	 * @param targetAddr The address (IP) of the client you want to call.
	 * @throws IOException
	 */
	public ClientSideClient(InetAddress targetAddr, BTClient parent) throws IOException {
		targetAddress = targetAddr;
		socket = new DatagramSocket(CLIENT_PORT);
		random = new Random();
		nextSeq = random.nextInt(75);
		this.parent = parent;
	}

	/**
	 * This method initializes the connection between clients.  It basically tells the other
	 * client to get ready for audio data.
	 * 
	 * @return A boolean that represents whether the call initialization was successful or not.
	 * @throws IOException
	 */
	public boolean initializeConnection() throws IOException {
		String response;

		Socket socket = new Socket(targetAddress, InitServer.INIT_PORT);

		// Send init string
		DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		outToServer.writeBytes("call init\n");

		// get response
		response = inFromServer.readLine();
		
		if (response == null) {
			
		} else {
			response = response.trim();
		}

		socket.close();

		// return true or false depending on whether init was successfuls
		if (response.compareToIgnoreCase("call accepted") == 0) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * This method basically tells the other client that this client wants to terminate
	 * the call.  It doesn't actually terminate anything on this end of the call, it just
	 * tells the other client we're about to stop sending data.
	 * 
	 * @return A boolean that represents whether the connection was successfully closed.
	 * @throws IOException
	 */
	public boolean closeConnection() throws IOException {
		String response;

		Socket socket = new Socket(targetAddress, InitServer.INIT_PORT);

		DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		outToServer.writeBytes("call terminate\n");

		response = inFromServer.readLine().trim();
		
		System.out.println(response);

		socket.close();

		if (response.compareToIgnoreCase("close accepted") == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This is the method that actually does the work of sending the data.  It is designed
	 * to be called from Valery's code (i.e. the code that does the audio encoding) and will
	 * send the data as soon as it's given it.
	 * 
	 * @param audioData An array of bytes that is the encoded audio data.
	 */
	public void send(byte[] audioData, int numBytesActual) {

		// add sequence number
		sendData = new byte[ClientSideServer.BYTES_PER_DATA + ClientSideServer.HEADER_LENGTH];
		sendData[0] = getNextSeq();
		
		byte[] length = Packer.unpackInt(numBytesActual);
		
		for (int i = 0; i < length.length; i++) {
			sendData[i + 1] = length[i];
		}

		for (int i = ClientSideServer.HEADER_LENGTH; i < sendData.length; i++) {
			sendData[i] = audioData[i - ClientSideServer.HEADER_LENGTH];
		}

		// send packet
		try {
			DatagramPacket packet = new DatagramPacket(sendData, sendData.length, targetAddress, ClientSideServer.CALL_PORT);
			socket.send(packet);
		} catch (Exception e) {
			System.err.println("Socket exception: connection closed");
			parent.killCall();
		}

	}

	/**
	 * This method is used to retrieve the next sequence number.  It makes sure
	 * the sequence number doesn't go over the size of a single byte (i.e. 128).
	 * 
	 * @return The next sequence number in byte form.
	 */
	private byte getNextSeq() {
		int tempInt = nextSeq;

		nextSeq++;
		nextSeq = nextSeq % 128;

		return ((byte) tempInt);
	}

	/**
	 * Terminates a call (stops sending audio data) and shut down the 
	 * ClientSideServer that is running.
	 *
	 */
	public void terminateCall() {
		String dummy = "dummy String";
		byte[] sendData = dummy.getBytes();

		InitServer.callActive = false;
		try {
			DatagramSocket finalSocket = new DatagramSocket(ClientSideServer.CALL_PORT - 1);
			DatagramPacket packet = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("localhost"), ClientSideServer.CALL_PORT);
			finalSocket.send(packet);
			finalSocket.close();
			socket.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
