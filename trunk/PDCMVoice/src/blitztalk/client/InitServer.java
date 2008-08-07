package blitztalk.client;

import blitztalk.client.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JOptionPane;

/**
 * This class contains the server that runs in the background all the time and receives call
 * requests.  This is also the location of the boolean which knows whether or not a call
 * is currently active.
 * 
 * Date 5/27/07
 * 
 * @author David Liechty
 *
 */

public class InitServer implements Runnable {
	//Port at which calls are initialized
	public static final int INIT_PORT = 4122;

	// TCP Server Socket that listens for call requests
	private ServerSocket initSocket;

	// Socket used to communicate with client making request
	private Socket socket;
	
	public static InetAddress localAddress;
	public static InetAddress targetAddress;

	// This boolean will be used when the client window (including buddy list) closes.
	// This value must be changed to false, and then a dummy packet should be sent to this
	// server so that the thread terminates on its own.
	private boolean stillRunning = true;

	// Whether or not a call is currently active.  If it is, don't accept additional calls
	public static boolean callActive = false;
	
	protected BTClient parentClient;

	
	/**
	 * Empty constructor
	 *
	 */
	public InitServer(BTClient parent) {
		this.parentClient = parent;
	}

	
	/**
	 * This method contains the server socket.  Basically it listens for TCP packets, and
	 * then passes them off to another thread that handles them.
	 */
	public void run() {
		
		try{
			initSocket = new ServerSocket(INIT_PORT);

			// listen for packets and pass to a ServerWorker thread
			while (stillRunning) {
				try {
					socket = initSocket.accept();
				} catch (Exception e) {
					System.out.println("Couldn't accept connection");
					return;
				}

				System.out.println("Packet received");
				
				ServerWorker worker = new ServerWorker(socket, this);
				(new Thread(worker)).start();
			}

			// after while loop terminates, close ServerSocket
			initSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method is designed to be called when the program closes- it just stops this
	 * server gracefully.
	 * 
	 * @throws Exception
	 */
	public void stopServer() {
		stillRunning = false;
		
		try {
			Socket socket = new Socket(localAddress, INIT_PORT);
			DataOutputStream finalSocket = new DataOutputStream(socket.getOutputStream());
			
			finalSocket.writeBytes("close");
			finalSocket.close();
		} catch (Exception e) { 
			System.err.println("Error sending close to InitServer");
		}
		
		try {
			initSocket.close();
		} catch (Exception e) {
			System.err.println("Error closing InitServer");
		}
	}
}

/**
 * This class is the worker for the ServerSocket.  It takes packets, parses them, and
 * then does the appropriate action.
 * 
 * 
 * Date: 5/27/07
 * 
 * @author David Liechty
 *
 */
class ServerWorker implements Runnable {

	Socket socket;

	// The data parsed from the packet.
	String query;
	
	InitServer parentServer;

	// The output stream we use to respond to the packet
	DataOutputStream response;

	/**
	 * Initialize the socket we'll use to communicate
	 * 
	 * @param socket The socket we get when a ServerSocket detects a connection
	 */
	public ServerWorker(Socket socket, InitServer parent) {
		this.socket = socket;
		this.parentServer = parent;
	}

	/**
	 * This method is what actually does the work on a packet.  It reads the data, matches it
	 * with a series of pre-defined commands, and then follows through with the appropriate action.
	 */
	public void run() {
		// Read data from packet
		try {
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			query = inFromClient.readLine().trim();
			response = new DataOutputStream(socket.getOutputStream());
			
			
			System.out.println(query);
			
			InitServer.localAddress = socket.getLocalAddress();
			InitServer.targetAddress = socket.getInetAddress();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}


		try{
			// Other client asks for a call
			if (query.compareToIgnoreCase("call init") == 0) {
				// If another call is not already active, then accept call; otherwise, refuse.
				Buddy caller = parentServer.parentClient.verifyCaller(socket.getInetAddress());
				if (InitServer.callActive || caller == null || !showAcceptDialog(caller.getName())) {
					response.writeBytes("call refused\n");

				} else {

					response.writeBytes("call accepted\n");
					System.out.println("Sent call accepted");
					parentServer.parentClient.acceptCall(socket.getInetAddress());
				}
				// Terminate call if other client requests it.
			} else if (query.compareToIgnoreCase("call terminate") == 0) {
				response.writeBytes("close accepted\n");
				parentServer.parentClient.acceptKillCall();
			}
			// Close socket after work is done.
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean showAcceptDialog(String name) {
		Object[] options = {"Accept", "Reject"};
		int n = JOptionPane.showOptionDialog(null,
					"Calling: " + name,
					"Accept Call",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE,
					null,     //don't use a custom Icon
					options,  //the titles of buttons
					options[0]); //default button title
		
		return (n == JOptionPane.YES_OPTION);
	}
}
