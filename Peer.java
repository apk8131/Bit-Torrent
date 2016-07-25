/**
 *   BitTorrent : simulation of bitTorrent protocol
 *   
 *   @author Ashutosh Katkar
 *           Mansi Nahar
 *           
 *    Peer.java : Peer that run without front end            
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Peer {

	// HashMap to store each file and its torrentInfo
	static HashMap<String, TorrentInfo> allMyTorrents = new HashMap<String, TorrentInfo>();

	/**
	 * parseTOrrent : read torrent file and store data in TorrentInfo object
	 * 
	 * @param fileName
	 * @return TorrentInfo
	 */
	public TorrentInfo parseTorrent(String fileName) {
		TorrentInfo torrentInfo = null;
		String torrentPath = fileName;
		try {

			FileReader fileReader = new FileReader(fileName);
			BufferedReader reader = new BufferedReader(fileReader);
			String line, trackerIP, infoString;
			int trackerPort;

			line = reader.readLine();
			trackerIP = line.substring(line.indexOf(':') + 1);

			line = reader.readLine();
			trackerPort = Integer
					.parseInt(line.substring(line.indexOf(':') + 1));

			line = reader.readLine();
			infoString = line + '\n';

			FileInfo fileInfo = null;
			String torrentFileName = "";
			int pieceSize, fileLength = 0;
			while ((line = reader.readLine()) != null) {
				if (line.contains("FileName")) {
					torrentFileName = line.substring(line.indexOf(':') + 1);
				} else if (line.contains("PieceSize")) {
					pieceSize = Integer.parseInt(line.substring(line
							.indexOf(':') + 1));
					fileInfo = new FileInfo(torrentFileName, pieceSize,
							fileLength);
				} else if (line.contains("FileLength")) {
					fileLength = Integer.parseInt(line.substring(line
							.indexOf(':') + 1));
				}

				infoString += line + "\n";
			}

			torrentInfo = new TorrentInfo(trackerIP, trackerPort, infoString,
					fileInfo, torrentPath);
			reader.close();

		} catch (FileNotFoundException e) {
			System.out.println("This file doesn't seem to exist");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Some problem in reading the file.");
			e.printStackTrace();
		}

		return torrentInfo;
	}

	public static void main(String args[]) {

		PeerListener listener = new PeerListener(Integer.parseInt(args[0]));
		listener.start();
		System.out.println("Listener started.");

		Peer peer = new Peer();
		// if peer want to download file pass torrent file as argument
		if (args.length == 2) {
			TorrentInfo torrentInfo = peer.parseTorrent(args[1]);
			torrentInfo.printInfo();
			allMyTorrents.put(torrentInfo.fileInfo.fileName, torrentInfo);

			// Create PieceRequester thread passing it the torrent info
			PieceRequester pieceRequester = new PieceRequester(torrentInfo);
			pieceRequester.start();
		}
	}
}
