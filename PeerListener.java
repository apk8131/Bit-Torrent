/**
 *   BitTorrent : simulation of bitTorrent protocol
 *   
 *   @author Ashutosh Katkar
 *           Mansi Nahar
 *           
 *    PeerListener.java : Continuous lister at peer which crate new thread for each TCP connection with other peers            
 */

import java.io.IOException;
import java.net.ServerSocket;

public class PeerListener extends Thread {

	int port;

	/**
	 * default constructor
	 * 
	 * @param port
	 */
	public PeerListener(int port) {
		this.port = port;
	}

	public void run() {
		ServerSocket ss;
		try {
			ss = new ServerSocket(port);
			while (true) {
				PeerResponder ps = new PeerResponder(ss.accept());
				ps.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
