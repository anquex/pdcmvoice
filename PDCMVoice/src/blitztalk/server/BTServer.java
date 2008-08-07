package blitztalk.server;
/**
 * BTServer class
 * Manages client connections and provides notifications to
 * clients
 * 
 * @author tcarney
 */

import java.net.*;
import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;

public class BTServer {

	// Listen for new connections on this port
	public static final int PORT = 4444;
	
	// List of clients
	// key is a String containing the username
	// value is a IMClientThread which is the connection to the client
	private Hashtable<String, BTClientThread> clients;
	
	// Socket connection
	private ServerSocket serverSocket = null;
	private boolean listen = true;
	
	// DND connection
	
        //--private DNDServer dnd;
	
	/**
	 * Starts up a new server
	 */
	public static void main(String[] args) {
		BTServer server = new BTServer();
		server.start();
	}
	
	/**
	 * Initialize server
	 */
	public BTServer() {
		clients = new Hashtable<String, BTClientThread>();
		//--dnd = new DNDServer();
	}
	
	/**
	 * Will open up a ServerSocket and listen for connections
	 * New connections will then be added to the system
	 */
	public void start() {
		// Attempt to open socket
		try {
			serverSocket = new ServerSocket(PORT);
		} catch (IOException e) {
			System.err.println("Could not listen on port: " + PORT);
			System.exit(-1);
		}
		
		// Listen for connection
		while (listen) {
			try {
				addClient(serverSocket.accept());
			} catch (IOException e) {
				System.err.println("Failed connection attempt on port: " + PORT);
			}
		}
	}
	
	/**
	 * Will create thread for new client and add it to the client list.
	 * 
	 * @param connection Socket connection to client
	 */
	public void addClient(Socket connection) {
		// Log new connection
		System.out.println("New connection:     " + connection);
		
		// Create thread with this server and client connection
		BTClientThread newClient = new BTClientThread(this, connection);
		
		// Run thread
		newClient.start();
	}
	
	/**
	 * Handle all input coming from clients
	 * 
	 * @param client   Client from which request came
	 * @param message  Message recieved from client
	 */
	public void processRequest(BTClientThread client, String message) {
		// Split message to read (trim as well)
		// request[0] = command
		// request[1] = username (optional)
		// request[2] = message (optional)
		String request[] = message.trim().split(" ", 3);
		
		// Must have at least a command
		if (request.length < 1) {
			System.err.println("Bad request from client: " + client);
		}
		
		// Handle command
		// If client doesn't request username first, then kick them off
		if (!request[0].equals("HELLO") && !client.hasName()) {
			client.disconnect();
		}
		// BYE: End connection
		else if (request[0].equals("BYE")) {
			client.disconnect();
		}
		// LIST: Send user list to client
		else if (request[0].equals("LIST")) {
			sendClientList(client);
		}
		// HELLO: check and assign/reject username
		else if (request[0].equals("HELLO") && request.length >= 2) {
			// Don't assign name to client that already has one
			if (client.hasName()) {
				client.println("ERR Already associated");
			} else {
				// Add client if valid
				//--String[] validateResult = dnd.validateUser(request[1], client);
				String[] validateResult = new String[]{"VALIDATE","dawdw","ddww"};

				// If user validate, add, otherwise return ERR
				if (validateResult != null) {
					// Check if name is taken
					if (clients.containsKey(validateResult[0])) {
						client.println("ERR User name assigned");
					} else { 
						clients.put(validateResult[0], client);
						client.setClientInfo(validateResult);
						System.out.println("Client Login:       " + client);
						notifyAllClients("HELLO " + client);
					}
				} else {
					client.println("ERR Bad password");
					System.out.println("Bad password, client: " + request[1]);
				}
			}
		}
		// MSG: Send message to request client, or send ERR back to client
		else if (request[0].equals("MSG") && request.length >= 3) {
			BTClientThread buddy = (BTClientThread) clients.get(request[1]);
			
			// Send ERR if not connected
			if (buddy == null) {
				client.println("ERR User (" + request[1] + ") is not connected");
			}
			else {
				buddy.println("MSG " + client.getUID() + " " + request[2]);
			}
		}
		else {
			System.out.println("Bad request from client: " + client);
			client.println("ERR Bad request");
		}
	}
	
	/**
	 * Remove specified client from server
	 * 
	 * @param name Username of client to be removed
	 */
	public void removeClient(String uid) {
		// Get all clients and send BYE message
		notifyAllClients("BYE " + uid);
		
		// Remove client, check for error
		BTClientThread client;
		if ((client = clients.remove(uid)) != null)
			System.out.println("Client Logout:      " + client);
		
		return;
	}
	
	/**
	 * Gets the list of usernames (keys) from the userlist hashtable
	 * @return List of names, seperated by spaces
	 */
	public String getClientList() {
		Enumeration list = clients.keys();
		String out = "";

		// Add each username to the string seperated by spaces
		while (list.hasMoreElements()) {
			out += list.nextElement() + " ";
		}
		
		return out.trim();
	}
	
	/**
	 * Will send a message to all connected clients
	 * @param message Message to be sent
	 */
	public void notifyAllClients(String message) {
		Enumeration list = clients.elements();
		
		while (list.hasMoreElements()) {
			((BTClientThread)list.nextElement()).println(message);
		}
	}
	
	/**
	 * Sends client list to client
	 * @param client Requesting client
	 */
	private void sendClientList(BTClientThread client) {
		// Send size
		client.println("LIST " + clients.size());
	
		// Send one "USER" line for each client
		Enumeration list = clients.elements();
		while (list.hasMoreElements()) {
			client.println("USER " + (BTClientThread)list.nextElement());
		}
	}

}
