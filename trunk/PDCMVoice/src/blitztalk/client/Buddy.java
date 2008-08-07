package blitztalk.client;

/**
 * Hold information on each client
 * 
 * @author tcarney
 *
 */
public class Buddy {
	private String name;
	private String uid;
	private String addr;
	
	/**
	 * Creates buddy
	 * @param info Client info string from server
	 */
	public Buddy(String info) {
		String[] result = info.split(",", 0);
		
		if (result.length < 3) {
			System.err.println("Error parsing buddy info");
			return;
		}
		
		uid = result[0];
		name = result[1];
		addr = result[2];
	}
	
	public String getName() {
		return name;
	}
	
	public String getUID() {
		return uid;
	}
	
	public String getAddress() {
		return addr;
	}
	
	public String toString() {
		return name;
	}
	
	public boolean equals(Object o) {
		if (o instanceof Buddy)
			return uid.equals(((Buddy) o).getUID());
		else
			return false;
	}
}
