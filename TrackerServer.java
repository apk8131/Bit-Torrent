/**
 *   BitTorrent : simulation of bitTorrent protocol
 *   
 *   @author Ashutosh Katkar
 *           Mansi Nahar
 *           
 *    TrackerServer.java : TrackerServer thread which is always running            
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class TrackerServer extends Thread {

	Socket s;
	BufferedReader br;
	PrintWriter pw;
	String infohash;

	/**
	 * default constructor
	 * 
	 * @param s
	 * @throws IOException
	 */
	public TrackerServer(Socket s) throws IOException {
		this.s = s;
		br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		pw = new PrintWriter(s.getOutputStream(), true);
		System.out.println(s.getPort());
		System.out.println(s.getRemoteSocketAddress());
	}

	/**
	 * add info about file and its swarm network (peers and seeders)
	 * addTrackerInfo
	 * 
	 * @throws IOException
	 */
	public void addTrackerInfo() throws IOException {
		String line = "";
		while ((line = br.readLine()) != null) {
			if (line.equals("EndOfInfo"))
				break;
			infohash = line;
			System.out
					.println("File with info hash " + infohash + " received.");
		}

		String Type = "seeder";
		String ip = s.getInetAddress().getHostAddress();
		// to make uniform all peers always listen on port 6500
		int port = 6500;
		Swarm swarm = new Swarm(Type, ip, port);
		Set<Swarm> temp = new HashSet<Swarm>();
		temp.add(swarm);
		Tracker.TrackerData.put(infohash, temp);

		pw.println("kill");
		System.out.println("Kill message sent");
	}

	/**
	 * 
	 * @throws IOException
	 */
	public void sendResponse() throws IOException {
		String line = "";
		while ((line = br.readLine()) != null) {
			if (line.equals("EndOfRequest"))
				break;
			infohash = line;
		}

		Set<Swarm> swarms = Tracker.TrackerData.get(infohash);

		pw.println("Response");

		boolean containsPeer = false;

		for (Swarm temp : swarms) {
			if (!(temp.ip.equals(s.getInetAddress().getHostAddress()))) {
				pw.println(temp.Type);
				pw.println(temp.ip);
				System.out.println(temp.Type + " IP: " + temp.ip);
				pw.println(temp.port);
			} else {
				containsPeer = true;
			}
		}

		// Add the peer who requested the file info to the list of peers for
		// that file
		if (!containsPeer) {
			Set<Swarm> peerAndSeederList = Tracker.TrackerData.get(infohash);

			String Type = "peer";
			String ip = s.getInetAddress().getHostAddress();
			int port = 6500;
			Swarm swarm = new Swarm(Type, ip, port);
			peerAndSeederList.add(swarm);
		}

		pw.println("EndOfResponse");
		System.out.println("End of response sent.");
	}

	/**
	 * updateSwarm
	 * 
	 * @throws IOException
	 */
	public void updateSwarm() throws IOException {
		String line = "";
		while ((line = br.readLine()) != null) {
			if (line.equals("EndOfUpdate"))
				break;
			infohash = line;
		}

		Set<Swarm> peerAndSeederList = Tracker.TrackerData.get(infohash);
		String clientIP = s.getInetAddress().getHostAddress();
		for (Swarm swarm : peerAndSeederList) {
			if (swarm.ip.equals(clientIP)) {
				swarm.Type = "seeder";
			}
		}

		pw.println("Updated");
	}

	public void run() {
		String in = "";
		boolean flag = false;
		while (!flag) {
			try {
				in = br.readLine();

				if (in.contains("NewFile")) {
					System.out.println("Line received" + in);
					addTrackerInfo();
				}

				if (in.contains("Request")) {
					sendResponse();
				}

				if (in.contains("UpdateInfo")) {
					updateSwarm();
				}
			} catch (Exception e) {
				try {
					flag = true;
					s.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}

	}
}
