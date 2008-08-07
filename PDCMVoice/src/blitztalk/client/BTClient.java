package blitztalk.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import blitztalk.audio.AudioControl;

/**
 * Main class for the client.  Hands server connection, listens for
 * notifications,  mantains UI, initiates calls.
 * 
 * @author tcarney
 */
public class BTClient implements ActionListener {
	// Connection info
	public static final int PORT = 4444;
	private String host;
	
	// Socket connection
	private Socket connection = null;
	private PrintWriter out = null;
	private BufferedReader in = null;
	private NotificationThread notif = null;

	// UI components
	private LoginUI loginWindow;
	private BuddyListUI buddyList;
	private CallUI callWindow;
	
	// Buddy list
	private BuddyList buddyListModel;

	// Own information
	private Buddy thisClient;

	// Call transport
	private InitServer connectionListener = null;
	private ClientSideClient connectionClient = null;
	private ClientSideServer connectionServer = null;
	
	// Call Audio
	private AudioControl audio;

	/**
	 * Starts the server, takes hostname from command line
	 */
	public static void main(String[] args) {
//		if (args.length > 0)
//			new BTClient(args[0]);
//		else
//			System.out.println("Usage: BTClient <hostname>");
	new BTClient("localhost");
        }

	/**
	 * Opens UIs and sets up listeners
	 */
	public BTClient(String host) {
		this.host = host;
		
		loginWindow = new LoginUI(this);
		loginWindow.setVisible(true);

		buddyListModel = new BuddyList();

		buddyList = new BuddyListUI(this, buddyListModel);
		buddyList.setVisible(false);
		
		audio = null;
	}

	/** 
	 * Handle action from UI
	 */
	public void actionPerformed(ActionEvent ev) {
		// Login button pressed
		if (ev.getActionCommand().equals("Login")) {
			if (login(loginWindow.getUsername(), loginWindow.getPassword())) {
				loginWindow.setVisible(false);
				loginWindow.clear();
				listenForNotifications();

				buddyList.setName("" + thisClient);
				buddyList.setVisible(true);
				
				// Listen for calls/notifications
				connectionListener = new InitServer(this);
				(new Thread(connectionListener)).start();
			}
		// Call button pressed
		} else if (ev.getActionCommand().equals("Call")) {
			Buddy toCall = (Buddy) buddyListModel.get(buddyList.getSelectedIndex());
			
			if (toCall.equals(thisClient)) {
				System.out.println("Can't call yourself.");
			} else {
				// Send test message through server
				out.println("MSG " + toCall.getUID() + " call");
				// Call
				placeCall(toCall);
			}
		// Logout button pressed
		} else if (ev.getActionCommand().equals("Logout")) {
			logout();
		} else if (ev.getActionCommand().equals("Hang up")) {
			killCall();
		}
	}

	/**
	 * Connects to server and logs user in
	 * 
	 * @param username Login alias
	 * @param password Password
	 * @return Success
	 */
	public boolean login(String username, String password) {
		// Check connection
		if (!isConnected())
			connect();

		// Send user name
		out.println("HELLO " + username);

		try {
			// Get server response
			String response = in.readLine();		
			String[] result = response.split(" ", 2);

			//System.out.println("Server said: " + response);
                        System.out.println(result[0]);
                        //--
//			if (result.length < 2 || !result[0].equals("VALIDATE")) {
//				System.err.println("Bad username");
//				return false;
//			}
//
//			// Send encrypted number to server
//			String en = DESEncryption.encrypt(password, result[1]);
//			out.println("VALIDATE " + en);
//
//			//  Get server response
//			response = in.readLine();
//			result = response.split(" ", 2);
//			//System.out.println("Server said: " + response);
//
//			if (result.length < 2 || !result[0].equals("HELLO")) {
//				System.err.println("Bad password");
//				return false;
//			}

			System.out.println("Logged on to server as: " + result[1]);
			thisClient = new Buddy(result[1]);
			getBuddyList();

			return true;

		} catch (IOException e) {
			System.err.println("I/O error with server.");
			return false;
		}
	}

	/**
	 * Loads buddy list from server
	 */
	private synchronized void getBuddyList() {
		// Empty list so we can refill it
		buddyListModel.removeAllElements();

		// Request buddy list from server
		out.println("LIST");

		try {
			// Get list size
			String[] response = in.readLine().split(" ", 2);

			int n = Integer.parseInt(response[1]);
			for (int i = 0; i < n; i++) {
				response = in.readLine().split(" ", 2);
				buddyListModel.addElement(new Buddy(response[1]));
			}
		} catch (IOException e) {
			System.err.println("I/O error with server.");
		}
	}

	/** 
	 * Connect to server
	 */
	public void connect() {

		try {
			// Open connection and I/O streams
			connection = new Socket(host, PORT);
			out = new PrintWriter(connection.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			// Check for WELCOME string
			String[] welcome = in.readLine().split(" ", 2);
			if (welcome.length < 1 || !welcome[0].equals("WELCOME")) {
				System.err.println("Server didn't announce itself.");
				disconnect();
			}

		} catch (UnknownHostException e) {
			// Exit if host cannot be found
			System.err.println("Unknown host: " + host);
			System.exit(-1);
		} catch (IOException e) {
			// Exit on I/O Error
			System.err.println("Error connecting with host: " + host);
			System.exit(-1);
		}
	}

	/**
	 * Disconnects from server
	 * Closes connections
	 * Exits program
	 */
	public void exit() {
		out.println("BYE");
		disconnect();
		System.exit(-1);
	}

	/**
	 * Disconnects from server
	 * Switches back to login window
	 */
	public void logout() {
		if (audio != null)
			audio.stop();
		
		if (InitServer.callActive)
			killCall();
		
		out.println("BYE");
		connectionListener.stopServer();
		connectionListener = null;
		disconnect();
		buddyList.setVisible(false);
		loginWindow.setVisible(true);
	}

	/**
	 * Close I/O streams + Socket
	 */
	public void disconnect() {
		try {
			if (isConnected()) {
				notif.stopListening();
				out.close();
				in.close();
				connection.close();
			}

			out = null;
			in = null;
			connection = null;
		} catch (IOException e) {
			System.err.println("Error closing connection");
		}
	}

	/**
	 * @return Status of connection
	 */
	public boolean isConnected() {
		if (connection == null)
			return false;
		else
			return connection.isConnected();
	}

	/**
	 * Start thread to listen for notifications
	 */
	private void listenForNotifications() {
		notif = new NotificationThread();
		notif.start();
	}

	/**
	 * This method starts a call from this address (as opposed to accepting a call from
	 * another client).
	 * 
	 * @param targetAddress The address of the client we are trying to call
	 */
	private void placeCall(Buddy call) {

		// For whatever reason, InetAddresses convert to Strings with a backslash in front
		// so it needs to be removed.
		String realAddress = call.getAddress().substring(1);

		// Starts the actual connection
		try{
			connectionClient = new ClientSideClient(InetAddress.getByName(realAddress), this);
			if (connectionClient.initializeConnection()) {
				
				audio = new AudioControl();
				
				connectionServer = new ClientSideServer(audio);
				(new Thread(connectionServer)).start();
				InitServer.callActive = true;
				
				// Open call window
				callWindow = new CallUI(this, call.getName());
				callWindow.setVisible(true);
				
				//Thread.sleep(85);
				audio.start(connectionClient);
			} else {
				System.out.println("Call rejected");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Approves call by verifying client and asking user
	 * @param incomingAddress
	 * @return
	 */
	public Buddy verifyCaller(InetAddress incomingAddress) {
		Buddy caller = buddyListModel.getByAddress("" + incomingAddress);
		
		return caller;
	}

	/**
	 * This method accepts a call that was requested from a different client.
	 * 
	 * @param incomingAddress The address from which a call is being requested.
	 */
	public void acceptCall(InetAddress incomingAddress) {
		Buddy caller = buddyListModel.getByAddress("" + incomingAddress);
		
		if (caller != null) {
			// Starts the actual connection
			try {
				connectionClient = new ClientSideClient(incomingAddress, this);
				audio = new AudioControl();
				connectionServer = new ClientSideServer(audio);
				(new Thread(connectionServer)).start();
				InitServer.callActive = true;
				
				//Thread.sleep(100);
				System.out.println("Sending data now........");
				audio.start(connectionClient);
				
				// Open call window
				callWindow = new CallUI(this, caller.getName());
				callWindow.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} else {
			System.out.println("Illegal caller!");
		}
	}

	/**
	 * This method terminates a call from the local client.
	 *
	 */
	public synchronized void killCall() {
		try {
		audio.stop();
		if (connectionClient != null) {
			connectionClient.closeConnection();
			connectionClient.terminateCall();
		}
		connectionClient = null;
		connectionServer = null;
		callWindow.dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method accepts a termination of a call from the other client.
	 *
	 */
	public void acceptKillCall() {
		try {
			audio.stop();
			connectionClient.terminateCall();
			connectionClient = null;
			connectionServer = null;
			callWindow.dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reads server nofications and handles them
	 * @author tcarney
	 */
	private class NotificationThread extends Thread {

		private boolean running = true;
		
		public void run() {
			String fromServer;

			try {
				while (running && (fromServer = in.readLine()) != null) {
					System.out.println(fromServer);

					// Handle HELLO and BYE notifications
					String[] result = fromServer.split(" ", 2);
					if (result.length > 1 && result[0].equals("HELLO")) {
						buddyListModel.addElement(new Buddy(result[1]));
					} else if (result.length > 1 && result[0].equals("BYE")) {
						buddyListModel.removeUID(result[1]);
					}
				}

				disconnect();
			} catch (IOException e) {
				// If we can't read from user, exit
				System.err.println("Error reading notifications from server");
			}
		}
		
		public void stopListening() {
			running = false;
		}
	}

}
