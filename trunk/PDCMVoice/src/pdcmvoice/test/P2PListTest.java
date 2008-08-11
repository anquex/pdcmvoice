/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.test;
import java.io.IOException;
import pdcmvoice.p2p.*;

/**
 *
 * @author marco
 */
public class P2PListTest {

    public P2PListTest(){

    }

    public static void main(String[] args) throws IOException{
        Client marco=new Client("marco", 5000);
        marco.connect(null);
        System.in.read();
        Client laura=new Client("laura", 5000);
        laura.connect(null);
        System.in.read();
        Client antonio=new Client("antonio", 5000);
        antonio.connect(null);
        System.in.read();
        Client zanella=new Client("Mr. Zanella", 5000);
        zanella.connect(null);
        System.in.read();
        marco.exit();
        System.in.read();
        System.exit(0);

    }
}


