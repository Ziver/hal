package zutil.network.threaded;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;



/**
 * A simple web server that handles both cookies and
 * sessions for all the clients
 * 
 * @author Ziver
 */
public class ThreadedUDPNetwork extends Thread{
	public static final int BUFFER_SIZE = 512;
	
	// Type of UDP socket
	enum UDPType{
		MULTICAST,
		UNICAST
	}
	protected final UDPType type;
	protected final int port;	
	protected DatagramSocket socket;
	protected ThreadedUDPNetworkThread thread = null;

	/**
	 * Creates a new unicast Clien instance of the class
	 * 
	 * @param thread is the class that will handle incoming packets
	 * @throws SocketException
	 */
	public ThreadedUDPNetwork(ThreadedUDPNetworkThread thread) throws SocketException{
		this.type = UDPType.UNICAST;
		this.port = -1;
		setThread( thread );
		
		socket = new DatagramSocket();
	}

	/**
	 * Creates a new unicast Server instance of the class
	 * 
	 * @param thread is the class that will handle incoming packets
	 * @param port is the port that the server should listen to
	 * @throws SocketException
	 */
	public ThreadedUDPNetwork(ThreadedUDPNetworkThread thread, int port) throws SocketException{
		this.type = UDPType.UNICAST;
		this.port = port;
		setThread( thread );
		
		socket = new DatagramSocket( port );
	}
	
	/**
	 * Creates a new multicast Server instance of the class
	 * 
	 * @param thread is the class that will handle incoming packets
	 * @param port is the port that the server should listen to
	 * @param multicast_addr is the multicast address that the server will listen on
	 * @throws IOException 
	 */
	public ThreadedUDPNetwork(ThreadedUDPNetworkThread thread, String multicast_addr, int port ) throws IOException{
		this.type = UDPType.MULTICAST;
		this.port = port;
		setThread( thread );
		
		// init udp socket
		MulticastSocket msocket = new MulticastSocket( port );
		InetAddress group = InetAddress.getByName( multicast_addr );
		msocket.joinGroup( group );	

		socket = msocket;
	}


	public void run(){
		try{
			while(true){
				byte[] buf = new byte[BUFFER_SIZE];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive( packet );
				if( thread!=null )
					thread.receivedPacket( packet, this );
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends the given packet
	 * 
	 * @param packet is the packet to send
	 * @throws IOException
	 */
	public synchronized void send( DatagramPacket packet ) throws IOException{
		socket.send(packet);
	}
	
	/**
	 * Sets the thread that will handle the incoming packets
	 * 
	 * @param thread is the thread
	 */
	public void setThread(ThreadedUDPNetworkThread thread){
		this.thread = thread;
	}
	
	
}