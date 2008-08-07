package blitztalk.client;

import javax.swing.DefaultListModel;

/**
 * Provides a list model to store buddys and display them in gui
 * 
 * @author tcarney
 */
public class BuddyList extends DefaultListModel {

	/**
	 * We might want to remove a buddy by UID
	 * @param uid
	 */
	public void removeUID(String uid) {
		for (int i = 0; i < size(); i++) {
			Buddy b = (Buddy) get(i);
			if (b.getUID().equals(uid)) {
				remove(i);
				return;
			}
		}
	}
	
	/**
	 * We want to verify that a call from addr is a valid client
	 * @param addr
	 * @return Buddy info for the caller
	 */
	public Buddy getByAddress(String addr) {
		for (int i = 0; i < size(); i++) {
			Buddy b = (Buddy) get(i);
			if (b.getAddress().equals(addr)) {
				return b;
			}
		}
		
		return null;
	}
}
