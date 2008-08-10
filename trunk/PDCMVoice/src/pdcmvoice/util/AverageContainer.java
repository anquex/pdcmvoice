/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.util;

/**
 *
 * @author marco
 */
public class AverageContainer{

        private int buffer[];
        private int n; //samples 
        private int index=0;
        private int received=0;
        private int min=0;

        public AverageContainer(int n, int k) {
            this.n=n;
            buffer=new int[n];
            min=k;
        }

        public synchronized void add(int i){
            buffer[index]=i;
            index= (index+1) % n;
            received++;
        }

        public int getAverage(){
            if(received<min) return -1;
            int sum=0;
            for (int i=0;i<n;i++)
                sum+=buffer[i];
                return sum/Math.min(received,n);
        }
    }//Average Container
