/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdcmvoice.test;

import java.io.IOException;
import java.net.UnknownHostException;
import pdcmvoice.client.Client;
import pdcmvoice.settings.AudioSettings;
import pdcmvoice.settings.ConnectionSettings;
import pdcmvoice.settings.TransmissionSettings;
import static pdcmvoice.impl.Constants.*;

/**
 *
 * @author marco
 */
public class SenderCall {

    public static void main(String[] args) throws UnknownHostException, IOException{
        ConnectionSettings c= new ConnectionSettings();
        c.setMaster(9000);
        c.setRTP(9001);
        c.setRTCP(9002);
        c.setRecovery(9003);
        new Client("Chiamante",
                new AudioSettings(),
                c,
                new TransmissionSettings()
                ).call("127.0.0.1", DEFAULT_MASTER_PORT);
    }

}