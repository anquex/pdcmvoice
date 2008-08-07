package blitztalk.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.SocketTimeoutException;

/**
 * DNDServer opens a connection to a DND server and provides methods
 * to authenticate a user and lookup a user by an alias
 * 
 * @author tcarney
 */
public class DNDServer {
	
	public static final String HOST = "dnd.dartmouth.edu";
	public static final int    PORT = 902;	
	public static final long TIMEOUT = 60000;
	
	// Socket connection
	private Socket connection = null;
	private PrintWriter out = null;
	private BufferedReader in = null;
	
	// Monitor activity and timeout connection
	private long lastActivity = 0;
	
	/**
	 * Initialize, load timeout thread
	 */
	public DNDServer() {
		DNDTimeoutThread watch = new DNDTimeoutThread();
		watch.start();
	}
	
	// For testing
	public static void main(String[] args) {
		DNDServer dnd = new DNDServer();
		if (dnd.connect()) {
			String result = dnd.lookupUser("tcarney");
			dnd.close();
			
			if (result != null) {
					System.out.println("uid: " + result);
			}
		} else {
			System.err.println("Could not connect to DND server");
		}
	}
	
	/**
	 * Opens connection and sets up I/O streams
	 * 
	 * @return Sucess of connection
	 */
	public synchronized boolean connect() {
//
//		try {
//			// Open connection and I/O streams
//			connection = new Socket(HOST, PORT);
//			connection.setSoTimeout(1000);
//
//			out = new PrintWriter(connection.getOutputStream(), true);
//			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//		} catch (UnknownHostException e) {
//			// Exit if host cannot be found
//			System.err.println("Unknown host: " + HOST);
//			return false;
//		} catch (IOException e) {
//			// Exit on I/O Error
//			System.err.println("Error connecting with host: " + HOST);
//			return false;
//		}
//
//		// Check connection by reading welcome message
//		// DND server should return "220 DND server ready."
//		// We'll just check for the "220"
//		try {
//			String welcome = in.readLine();
//
//			String[] result = welcome.split(" ", 2);
//
//			if (result.length > 0 && result[0].equals("220")) {
//				System.out.println("DND Server:         Connection opened.");
//			} else {
//				System.out.println("Error connecting with DND server");
//				close();
//				return false;
//			}
//		} catch (SocketTimeoutException e) {
//			System.err.println("DND server not responding.");
//			close();
//			return false;
//		} catch (IOException e) {
//			System.err.println("Error reading from DND server");
//			close();
//			return false;
//		}
//
//		lastActivity = System.currentTimeMillis();
		return true;
	}
	
	/**
	 * Validates a client based on username they pass in
	 * @param username Login alias
	 * @param client Client to talk with since this is multi step process
	 * @return Array of returned results, null if failed
	 */
	public synchronized String[] validateUser(String username, BTClientThread client) {
		// Check to see if we're connected
		if (!isConnected()) {
			connect();
		}
		
		lastActivity = System.currentTimeMillis();
		
		// Start validation
		out.println("VALIDATE " + username + ",UID Name Email");
		
		try {
			// Get response
			String response = in.readLine();
			String[] result = response.split(" ", 2);
			
			// Check for failure, print response
			if (result.length > 1 && result[0].equals("300")) {
				//System.out.println("Validate returned: " + response);
			} else {
				System.err.println("Validate failed: " + response);
				return null;
			}
			
			// Request password from user
			client.println("VALIDATE " + result[1]);
			String[] passResponse = client.readLine().split(" ", 0);
			
			// We have passed validate now send pass
			if (passResponse.length > 1 && passResponse[0].equals("VALIDATE")) {
				//System.out.println("user returned: " + passResponse[1]);
				out.println("PASE " + passResponse[1]);
			} else {
				System.err.println("Error retriving password from user");
				return null;
			}
			
			// Get respose
			response = in.readLine();
			result = response.split(" ", 2);
			
			if (result.length > 1 && result[0].equals("101")) {
				String[] nm = result[1].split(" ", 0);
				if (nm.length < 2) {
					System.err.println("Error retriving results from DND server");
					return null;
				}
				
				// Get return value numbers
				//int n = Integer.parseInt(nm[0]);
				int m = Integer.parseInt(nm[1]);
				
				String[] fields = new String[m];
				
				for (int i = 0; i < m; i++) {
					String[] inputArgs = in.readLine().split(" ", 2);
					if (inputArgs.length < 2 || !inputArgs[0].equals("110")) {
						System.err.println("Error retriving results from DND server");
						return null;
					} else {
						fields[i] = inputArgs[1];
					}
				}
				
				// Check to see if server id done
				String[] done = in.readLine().split(" ", 2);
				if (done.length < 1 || !done[0].equals("200"))  {
					System.err.println("Error retriving results from DND server");
					close();
				}
				
				return fields;
			}
			else if (result.length > 0 && result[0].equals("530")) {
				System.err.println("Bad password: " + response);
				return null;
			} else {
				System.err.println("Error validating user: " + response);
				return null;
			}
		
		} catch (SocketTimeoutException e) {
			System.err.println("DND server not responding.");
			close();
			return null;
		} catch (IOException e) {
			System.err.println("I/O Error with DND server");
			close();
			return null;
		}
	}
	
	/**
	 * Looks up a user by name and returns UID if valid
	 * @param username Name to look up
	 * @return UID if valid, null if not found
	 */
	public synchronized String lookupUser(String username) {
		String uid = null;
		
		// Check to see if we're connected
		if (!isConnected()) {
			connect();
		}
		
		lastActivity = System.currentTimeMillis();
		
		// Send look up request
		out.println("LOOKUP " + username + ",UID");
		
		// Get response
		try {
			String queryResponse = in.readLine();
			System.out.println("query returned: " + queryResponse);
			String[] result = queryResponse.split(" ", 2);
			
			String[] nm = null;
			if (result.length > 1 && result[0].equals("101")) {
				nm = result[1].split(" ", 0);
				if (nm.length < 2) {
					System.err.println("Error retriving results from DND server");
					return null;
				}
			} else {
				System.err.println("Invalid lookup returned");
				return null;
			}
			
			// Get return value numbers
			int n = Integer.parseInt(nm[0]);
			int m = Integer.parseInt(nm[1]);
			
			// Should only return 1 user and 1 field
			if (n != 1 || m != 1) {
				System.err.println("Invalid lookup returned");
				close();
				return null;
			}
			
			// Read result
			String[] inputArgs = in.readLine().split(" ", 2);
			if (inputArgs.length < 2 || !inputArgs[0].equals("110")) {
				System.err.println("Error retriving results from DND server");
				return null;
			} else {
				uid = inputArgs[1];
			}
			
			// Check to see if server id done
			String[] done = in.readLine().split(" ", 2);
			if (done.length < 1 || !done[0].equals("200"))  {
				System.err.println("Error retriving results from DND server");
				close();
				return null;
			}
		} catch (IOException e) {
			System.err.println("I/O error with DND server");
			close();
			return null;
		}
		
		return uid;
	}
	
	/** 
	 * Closes I/O streams and socket connection
	 */
	public synchronized void close() {
		try {
			in.close();
			out.close();
			connection.close();
			in = null;
			out = null;
			connection = null;
		} catch (IOException e) {}
		System.out.println("DND Server:         Connection closed.");
	}
	
	/**
	 * @return  Connection state
	 */
	public boolean isConnected() {
		if (connection == null)
			return false;
		else
			return connection.isConnected();
	}
	
	/**
	 * If timeout expired close connection
	 */
	public void checkTimeout() {
		if (isConnected() && (System.currentTimeMillis() - lastActivity) > TIMEOUT)
			close();
	}
	
	/**
	 * Background thread to monitor timeout
	 * @author tcarney
	 */
	private class DNDTimeoutThread extends Thread {
		
		/**
		 * Check to see if timeout has expired every TIMEOUT / 2 ms
		 */
		public void run() {
			while (true) {
				try {
					Thread.sleep(TIMEOUT / 2);
				} catch (InterruptedException e) {}
				checkTimeout();
			}
		}
	}
}
