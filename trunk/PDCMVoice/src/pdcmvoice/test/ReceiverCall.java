/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.test;

import pdcmvoice.client.Client;
import static pdcmvoice.impl.Constants.*;

/**
 *
 * @author marco
 */
public class ReceiverCall {

    public static void main(String[] args){
        new Client("Ricevitore").start();
    }

}
