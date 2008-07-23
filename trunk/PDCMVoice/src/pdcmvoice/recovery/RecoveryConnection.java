package pdcmvoice.recovery;

import jlibrtp.RTPSession;

/*
creata da VoiceSession ??

Stato RTPSession
info RTCP: devo creare una classe che impl RTCPAppIntf che usa i parametri descritti dall'interfaccia come il packet loss

pkt speex e pcm lunghi 20 ms
header TCP è   byte
 */

/**
 * 
 * @author Antonio
 */

import java.net.Socket;

public class RecoveryConnection
{
    private Socket server; //stabilito nel main
    private Socket client; //stabilito nel main
	private RecoveryCollection local;
	private RecoveryCollection remote;
	public RTPSession rtpSession;
	public boolean debug;
	
    public RecoveryConnection(Socket server, RecoveryCollection local, Socket client, RecoveryCollection remote, RTPSession rtpSession, boolean debug)
	{
        this.client = client;
        this.server = server;
        this.local = local;
        this.remote = remote;
        this.rtpSession = rtpSession;
        this.debug = debug;
		
	}
    
    public Socket getServerSocket()
    {
        return this.server;
    }
	
    public Socket getClientSocket()
    {
        return this.client;
    }
    
    public RecoveryCollection getLocalCollection()
    {
        return this.local;
    }
    
    public RecoveryCollection getRemoteCollection()
    {
        return this.remote;
    }
    
    public RTPSession getRtpSession()
    {
        return this.rtpSession;
    }
    
    
}
