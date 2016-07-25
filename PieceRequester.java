/**
 *   BitTorrent : simulation of bitTorrent protocol
 *   
 *   @author Ashutosh Katkar
 *           Mansi Nahar
 *           
 *    PieceRequester.java : Peer request the required chunk             
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.JLabel;

public class PieceRequester extends Thread {

	TorrentInfo torrentInfo;

	/**
	 * Default Constructor
	 * 
	 * @param torrentInfo
	 */
	public PieceRequester(TorrentInfo torrentInfo) {
		this.torrentInfo = torrentInfo;
	}

	/**
	 * getSwarmData : get all available peers and seeders info from tracker
	 * 
	 * @param torrentInfo
	 */
	public void getSwarmData(TorrentInfo torrentInfo) {
		String messageType = "Request";
		String messageEnd = "EndOfRequest";
		String infoHash = torrentInfo.infoHash;
		try {
			Socket socket = new Socket(torrentInfo.trackerIP,
					torrentInfo.trackerPort);
			PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			writer.println(messageType);
			writer.println(infoHash);
			writer.println(messageEnd);

			String line = "";

			while (true) {
				line = reader.readLine();
				System.out.println("Response from tracker " + line);
				if (line.contains("EndOfResponse")) {
					System.out.println("EndOfResponse message received");
					socket.close();
					break;
				} else if (line.contains("Response")) {

					while (!(line = reader.readLine())
							.contains("EndOfResponse")) {
						if (line.contains("seeder")) {
							System.out.println("It's a seeder!");
							torrentInfo.seeders.put(reader.readLine(),
									Integer.parseInt(reader.readLine()));
						} else if (line.contains("peer")) {
							System.out.println("It's a peer!");
							torrentInfo.peers.put(reader.readLine(),
									Integer.parseInt(reader.readLine()));
						}
					}

					socket.close();
					break;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * updateTracker : updates tracker when peer become seeder
	 * 
	 * @param torrentInfo
	 */
	public void updateTracker(TorrentInfo torrentInfo) {
		String messageType = "UpdateInfo";
		String messageEnd = "EndOfUpdate";
		String infoHash = torrentInfo.infoHash;
		try {
			Socket socket = new Socket(torrentInfo.trackerIP,
					torrentInfo.trackerPort);
			PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			writer.println(messageType);
			writer.println(infoHash);
			writer.println(messageEnd);

			String line = "";

			while (true) {
				line = reader.readLine();
				if (line.contains("Updated")) {
					System.out.println("Updated message received");
					socket.close();
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * writeInFile : to store number of chunks already received when peer
	 * recover from failure
	 * 
	 * @param fileName
	 * @param pieceReceived
	 */
	public void writeInFile(String fileName, String pieceReceived) {

		File dir = new File("meta");
		if (!dir.exists()) {
			dir.mkdir();
		}

		fileName = fileName.substring(0, fileName.indexOf('.'));
		fileName = "meta/" + fileName + ".meta";

		if (pieceReceived.contains("seeder")) {
			try {
				Files.delete(Paths.get(fileName));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				Files.write(Paths.get(fileName), pieceReceived.getBytes(),
						StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			} catch (IOException e) {
			}
		}
	}

	public void run() {

		// store current Peers (IP)
		ArrayList<String> threadCreated = new ArrayList<String>();

		writeInFile(torrentInfo.fileInfo.fileName,
				torrentInfo.fileInfo.fileName + " ");

		while (!torrentInfo.piecesNeeded.isEmpty()) {

			System.out.println("We still need to download pieces");

			System.out.println(torrentInfo.piecesNeeded.size()
					+ " number of more pieces needed.");

			getSwarmData(torrentInfo);

			// iterate through all peers available and create connection
			Iterator<Entry<String, Integer>> peerIterator = torrentInfo.peers
					.entrySet().iterator();
			while (peerIterator.hasNext()) {
				Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>) peerIterator
						.next();
				try {
					RequesterThread thread = new RequesterThread(pair.getKey(),
							pair.getValue(), true, torrentInfo, threadCreated);
					if (!threadCreated.contains(pair.getKey())) {
						System.out
								.println("Starting connection with "
										+ pair.getKey() + " at port "
										+ pair.getValue());
						thread.start();
						threadCreated.add(pair.getKey());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// iterate through all seeders available and create connection
			Iterator<Entry<String, Integer>> seederIterator = torrentInfo.seeders
					.entrySet().iterator();
			while (seederIterator.hasNext()) {
				Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>) seederIterator
						.next();
				try {
					RequesterThread thread = new RequesterThread(pair.getKey(),
							pair.getValue(), false, torrentInfo, threadCreated);
					if (!threadCreated.contains(pair.getKey())) {
						System.out
								.println("Starting connection with "
										+ pair.getKey() + " at port "
										+ pair.getValue());
						thread.start();
						threadCreated.add(pair.getKey());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			try {
				// check after every 10 sec with tracker if any new peer or
				// seeder is added
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		JLabel l = MainPeer.displays.get(torrentInfo.fileInfo.fileName);
		l.setText(torrentInfo.fileInfo.fileName + " download completed !!");

		updateTracker(torrentInfo);
		writeInFile(torrentInfo.fileInfo.fileName, "seeder");

		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
