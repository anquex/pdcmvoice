/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.util;

/**
 *
 * @author marco
 */
public class CircularBuffer{
    
    private byte buffer[];
    int readpos,writepos,count,size;
        
    public  CircularBuffer(){
        this(1024);
    }
    public CircularBuffer(int length){
        buffer= new byte[length+1];
        size=length;
    }
    
    public void add(byte b){
        
    }
    
    public void remove(byte b){
        
    }
    
    public int available(){
        return count;
    }
    public int writable(){
        return getSize()-available();
    }
    public int getSize(){
        return size;
    }
    public boolean isFull(){
        return (writepos+1==readpos);
        
    }
    public boolean isEmpty(){
        return (readpos==writepos);
    }
    
    

}
