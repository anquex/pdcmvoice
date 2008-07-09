/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.util.tests;

import pdcmvoice.util.CircularByteBuffer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author marco
 */
public class CircularByteBufferTest {
    CircularByteBuffer c;
    byte[] buffer;
    InputStream in;
    OutputStream out;
    
    public CircularByteBufferTest(){
        c=new CircularByteBuffer(321);
        buffer=new byte[400];
        
        in=c.getInputStream();
        out=c.getOutputStream();      
        Reader r=new Reader();
        Writer w=new Writer();
        r.start();
        w.start();
        
        
        
    }
    
    class Reader extends Thread{
        
        public void run() {
            try {
                sleep(200);
                byte[] readB=new byte[640];
                in.read(readB, 0, 80);
                in.read(readB, 0, 80);
                in.read(readB, 0, 640-160);
                
            } catch (IOException ex) {
                Logger.getLogger(CircularByteBufferTest.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(CircularByteBufferTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    class Writer extends Thread{
        
        public void run() {
            try {
                sleep(100);
                out.write(buffer);
                out.write(buffer);
                
                
                
            } catch (InterruptedException ex) {
                Logger.getLogger(CircularByteBufferTest.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {}
        
        }
    }
    
    public static void main(String[] args){
        CircularByteBufferTest t=new CircularByteBufferTest();
    }

}
