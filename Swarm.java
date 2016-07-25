/**
 *   BitTorrent : simulation of bitTorrent protocol
 *   
 *   @author Ashutosh Katkar
 *           Mansi Nahar
 *           
 *    Swarm.java : helper class to store all info about peer or seeder             
 */
import java.util.ArrayList;

public class Swarm {
    String Type;
    String ip;
    int port;
	ArrayList <Integer> chunks;
	int bytesLeft;
	
	/**
	 * Default constructor
	 * @param Type
	 * @param ip
	 * @param port
	 */
	public Swarm(String Type, String ip, int port) {
		this.Type = Type;
		this.ip = ip;
		this.port = port;
		chunks = new ArrayList<Integer>();
	}
	
}
