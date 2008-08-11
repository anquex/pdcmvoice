/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.p2p;

/**
 *
 * @author marco
 */
public class Constants {
    public static final int P2PPORT = 5000;

    public static void out(String s){
        System.out.println(s);
    }
    public static String joinMessage(UserNode u){
        return "USERJOIN:"+u.toString();

    }
    public static String leaveMessage(UserNode u){
        return "USERLEAVE:"+u.toString();

    }
    public static String disconnect(){
        return "BYE";

    }

}
