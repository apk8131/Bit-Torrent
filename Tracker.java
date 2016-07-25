/**
 *   BitTorrent : simulation of bitTorrent protocol
 *   
 *   @author Ashutosh Katkar
 *           Mansi Nahar
 *           
 *    Tracker.java : Tracker which creates tracker server which is always on            
 */	
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Set;

public class Tracker {

	// map for each file and its swarm
	public static HashMap<String, Set<Swarm>> TrackerData = new  HashMap<String, Set<Swarm>>();
	public static void main(String [] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        ServerSocket ss = new ServerSocket(port);
        
        while(true) {
       	 TrackerServer ts = new TrackerServer(ss.accept());
       	 ts.start();
        }
	}
	
}
