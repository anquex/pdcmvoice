/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.util.tests;

/**
 *
 * @author marco
 */
public class ShortOverFlowTest {
    
    public static void main (String[] args){
        short s = Short.MAX_VALUE;
        System.out.println(s);
        short a=1;
        s=(short) (s + a);
        System.out.println(s);
        s=Short.MIN_VALUE;
        System.out.println(s);
    }

}
