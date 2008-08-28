/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.p2p;

import javax.swing.DefaultListModel;

/**
 *
 * @author Laura
 */

//List Model per la mia lista di utenti
//aggiunge e rimuove elemetni dalla lista
public class UserNodeListModel extends DefaultListModel{
    /**
     * Rimuove uno UserNode 
     * @param user UserNode da rimuovere
     */
    
    public void removeUserNode(UserNode user){
        for (int i = 0 ; i < size() ; i++){
            UserNode u = (UserNode)get(i);
            if((u.getUserName()).equals(user))
                super.removeElement(u);
        }
    }
    /**
     * Aggiuge uno UserNode alla list
     * @param user UserNode da aggiungere
     */
    public void addUser(UserNode user){
        super.addElement(user);       
    }
    /**
     * 
     * @return il numero di elementi del list model della lista
     */
    public int size(){
        return super.size();
    }
    
  
 
}
