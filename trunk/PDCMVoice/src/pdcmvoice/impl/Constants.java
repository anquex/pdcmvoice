/*
 *	Constants.java
 */

/*
 * Copyright (c) 2004 by Florian Bomers
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package pdcmvoice.impl;
import javax.sound.sampled.*;


public class Constants {
    
    public Constants(){};
    
    // Audio Constants
    public static final int DIR_MIC = 0;
    public static final int DIR_SPK = 1;

    public static boolean DEBUG = true;
    public static boolean VERBOSE = true;

    public static final int FORMAT_CODE_SPEEX_NB=1;
    public static final int FORMAT_CODE_SPEEX_WB=2;
    public static final int FORMAT_CODE_iLBC=3;
    

    public static final String[] FORMAT_NAMES={
	"Speex CBR",
	"iLBC",
    };

    public static final int[] FORMAT_CODES={
	FORMAT_CODE_SPEEX_NB,
        FORMAT_CODE_SPEEX_WB,
	FORMAT_CODE_iLBC,

    };
    
    public static final int DEFAULT_FORMAT_CODE = FORMAT_CODE_SPEEX_NB;

    public static final int[] BUFFER_SIZE_MILLIS = {
    	30, 40, 50, 70, 85, 100, 130, 150, 180, 220, 400
    };
    public static final String[] BUFFER_SIZE_MILLIS_STR = {
    	"30", "40", "50", "70", "85", "100", "130", "150", "180", "220", "400"
    };
    
    public static final int BUFFER_SIZE_INDEX_DEFAULT = 2;


    public static final String CONNECTION_PROPERTY = "CONNECTION";
    public static final String AUDIO_PROPERTY = "AUDIO";

    public static final int PROTOCOL_MAGIC = 0x43484154;
    public static final int PROTOCOL_VERSION = 1;
    public static final int PROTOCOL_ACK = 1001;
    public static final int PROTOCOL_ERROR = 1002;

    
    //NETWORK SETTINGS
    
    // Socket options
    public static final boolean TCP_NODELAY = false;
    // -1 means do not set the value
    public static final int TCP_RECEIVE_BUFFER_SIZE = 1024;
    public static final int TCP_SEND_BUFFER_SIZE = 1024;

    public static final int DEFAULT_MASTER_PORT = 8765;
    public static final int DEFAULT_RTP_PORT = 8766;
    public static final int DEFAULT_RTCP_PORT = 8767;
    public static final int DEFAULT_RECOVERY_PORT = 8769;
    
    public static final int[] SPEEX_QUALITY=
    {
        0,1,2,3,4,5,6,7,8,9,10
    };
    
    public static final int DEFAULT_SPEEX_QUALITY_INDEX = 5;
    
    public static final int PAYLOAD_iLBC=96;
    public static final int PAYLOAD_SPEEX=97;
    public static final int PAYLOAD_iLBC_RDT=98;
    public static final int PAYLOAD_SPEEX_RDT=99;
    
    
    //  FEC ENABLE?

    public static void out(String s) {
	System.out.println(s);
    }
    public static void out(int n) {
	System.out.println(""+n);
    }
}
