/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.p2p;

import java.net.InetAddress;

/**
 *
 * @author marco
 */
public class UserNode implements Comparable<UserNode>{

    private String username;
    private int    masterport;
    private String address;

    public UserNode(String u, String a,int m){
        username=u;
        masterport=m;
        address=a;
    }
    public UserNode(String u, String a,String m){
        masterport=Integer.parseInt(m);
        username=u;
        address=a;
    }

    public int compareTo(UserNode u){
        return username.compareTo(u.username);
    }

    public String toString(){
        return username+","+address+","+masterport;
    }

    public static UserNode getUserNode(String s){
        UserNode  u = null;
        String[] ss= s.split(",",0);
        int n=Integer.parseInt(ss[2]);
        if (ss.length==3){
            u= new UserNode(ss[0], ss[1], n);
        }
        return u;
    }
    public boolean equals(UserNode u){
        return username.equals(u.username);
    }

}
