
package blitztalk.client;
import java.net.*;
import java.io.*;

/**
 * Connects to server and provides I/O
 * Does not parse protocol, simply sends it to server, and prints server output
 * to user.  This allows the user to interact with the protocol.
 * 
 * Does not handle calls, purely for testing server protocol
 * 
 * @author tcarney
 */
public class BTClientCMD {
	
	// Connection info
	public static final int PORT = 4444;
	public static final String HOST = "localhost";
	
	// Socket connection
	private Socket connection = null;
	private PrintWriter out = null;
	private BufferedReader in = null;
	
	private String getPass = null;
	/**
	 * Opens a test connection
	 * @param args
	 */
	public static void main(String[] args) {
		BTClientCMD client = new BTClientCMD();
		client.connect();
	}	
	
	/** 
	 * Connect to server
	 *
	 * @throws IOException
	 */
	public void connect() {

		try {
			// Open connection and I/O streams
			connection = new Socket(HOST, PORT);
			out = new PrintWriter(connection.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		} catch (UnknownHostException e) {
			// Exit if host cannot be found
			System.err.println("Unknown host: " + HOST);
			System.exit(-1);
		} catch (IOException e) {
			// Exit on I/O Error
			System.err.println("Error connecting with host: " + HOST);
			System.exit(-1);
		}
		
		// Setup input from user
		InputThread inputThread = new InputThread();
		inputThread.start();
		String fromServer;
		
		// Read from server
		try {
			while ((fromServer = in.readLine()) != null) {
				System.out.println(fromServer);
				
				String[] result = fromServer.split(" ", 0);
				if (result.length > 1 && result[0].equals("VALIDATE")) {
					getPass = result[1];
				}
			}
		} catch (IOException e) {
			System.err.println("Error reading from server");
			System.exit(-1);
		}
		
		inputThread.closeInput();
		disconnect();
	}
	
	public void disconnect() {
		// Close I/O streams + Socket
		try {
			out.close();
			in.close();
			connection.close();
		} catch (IOException e) {
			System.err.println("Error closing connection");
		}
	}
	
	/**
	 * Waits for user input
	 * @author tcarney
	 */
	public class InputThread extends Thread {
		
		private boolean getInput = true;
		
		public void run() {
			// Open stdio
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			String fromUser;
			
			try {
				while (getInput && (fromUser = stdIn.readLine()) != null) {
						// Do process password for encryption
						if (getPass != null) {
							fromUser = "VALIDATE " + DESEncryption.encrypt(fromUser, getPass);
							getPass = null;
						}
						out.println(fromUser);
				}
				
				stdIn.close();
				
				disconnect();
			} catch (IOException e) {
				// If we can't read from user, exit
				System.err.println("Error reading input");
				System.exit(-1);
			}
		}
		
		/**
		 * Tell thread to stop reading input
		 * Need to fix the readLine() since you must send a line to exit the program
		 */
		public void closeInput() {
			getInput = false;
		}
	}

}
