package blitztalk.server;
import java.net.*;
import java.io.*;

/**
 * Thread to handle all client specific functions.
 * Controls connection and Input/Output
 * Holds name and uid
 * 
 * @author tcarney
 *
 */
public class BTClientThread extends Thread {
	
	private BTServer server;			// Server from which this thread started
	private Socket connection = null;	// Connection to client
	private PrintWriter out;			// Output stream
	private BufferedReader in;			// Input stream
	
	private String name;				// User name
	private String uid;
	private boolean listen;
	
	/**
	 * Create new thread for client
	 * 
	 * @param socket Connection to client
	 * @param parent Server to which client connected.
	 */
	public BTClientThread(BTServer parent, Socket socket) {
		super("IMClientThread");
		connection = socket;
		server = parent;
		name = null;
		listen = false;
	}
	
	/**
	 * Starts thread by opening I/O streams and waiting for input
	 * Closes the connection on request or lost client.
	 */
	public void run() {
		try {
			// Open I/O streams
			out = new PrintWriter(connection.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			
			String input;
			
			// Welcome client
			out.println("WELCOME blitzTalk v0.1.0");
			listen = true;
			
			// Read input from client, send to server to process
			while (listen && (input = in.readLine()) != null) {
				server.processRequest(this, input);
			}
	
		} catch (IOException e) {
			System.err.println("I/O error with client.");
		}
		
		// Disconnect if we lose client
		if (listen)
			disconnect();
	}
	
	/**
	 * Disconnects client by removing from server and closing all open
	 * connections and streams.
	 *
	 */
	public void disconnect() {
		listen = false;
		// Remove from client list
		if (uid != null)
			server.removeClient(uid);
		
		// Close I/O streams + Socket
		try {
			out.close();
			in.close();
			connection.close();
			
			System.out.println("Connection closed:  " + connection);
		} catch (IOException e) {
			System.err.println("Error closing connection: " + connection);
		}
	}
	
	/**
	 * Send string to client
	 * @param output Message to send to client
	 */
	public synchronized void println(String output) {
		out.println(output);
	}
	
	/**
	 * Read line from client
	 * 
	 * @return Line from client
	 * @throws IOException
	 */
	public synchronized String readLine() throws IOException {
		return in.readLine();
	}
	
	/**
	 * Sets client infor
	 * @param info Array of parameters
	 */
	public void setClientInfo(String[] info) {
		if (info.length > 1) {
			uid = info[0];
			name = info[1];
		}
	}
	
	/**
	 * Client has name
	 * @return
	 */
	public boolean hasName() {
		return (name != null);
	}
	
	public String getUID() {
		return uid;
	}
	
	/**
	 * Client is represented as user name
	 */
	public String toString() {
		if (name == null)
			return "[not associated]";
		else
			return uid + "," + name + "," + connection.getInetAddress();
	}
}
