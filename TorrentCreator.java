/**
 *   BitTorrent : simulation of bitTorrent protocol
 *   
 *   @author Ashutosh Katkar
 *           Mansi Nahar
 *           
 *    TorrentCreater.java : helper class to create torrent file            
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import javax.xml.bind.DatatypeConverter;

public class TorrentCreator {

	public TorrentCreator() {
	}

	/**
	 * createTorrentFile
	 * 
	 * @param fileName
	 * @param info
	 * @param trackerIP
	 * @param trackerPort
	 */
	public void createTorrentFile(String fileName, ArrayList<FileInfo> info,
			String trackerIP, int trackerPort) {
		try {
			fileName = fileName + ".torrent";
			PrintWriter writer = new PrintWriter(fileName);

			writer.append("Ip:" + trackerIP + "\n");
			writer.append("Port:" + trackerPort + "\n");
			writer.append(getInfoString(info));

			writer.close();
		} catch (FileNotFoundException e) {
			System.out.println("File doesn't exist!");
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param info
	 * @param trackerIP
	 * @param trackerPort
	 */
	public void infomTracker(ArrayList<FileInfo> info, String trackerIP,
			int trackerPort) {

		String infoHash = getHash(getInfoString(info));
		String messageType = "NewFile";
		String messageEnd = "EndOfInfo";

		try {
			Socket socket = new Socket(trackerIP, trackerPort);
			PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			writer.println(messageType);
			writer.println(infoHash);
			writer.println(messageEnd);

			System.out.println("Message written on " + trackerIP + " "
					+ trackerPort);

			String line = "";

			while (true) {
				line = reader.readLine();
				if (line.contains("kill")) {
					System.out.println("Kill message received");
					socket.close();
					break;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * get all info to be store in torrent file getInfoString
	 * 
	 * @param info
	 * @return
	 */
	public String getInfoString(ArrayList<FileInfo> info) {
		String infoString = "Files:\n";
		for (int i = 0; i < info.size(); ++i) {
			infoString += "FileName:" + info.get(i).fileName + "\n";
			infoString += "FileLength:" + info.get(i).fileLength + "\n";
			infoString += "FileHash:" + info.get(i).fileHash + "\n";
			infoString += "Pieces:" + info.get(i).noOfPieces + "\n";
			infoString += "PieceSize:" + info.get(i).pieceSize + "\n";
		}
		return infoString;
	}

	/**
	 * getHash
	 * 
	 * @param originalString
	 * @return SH1 value
	 */
	String getHash(String originalString) {

		MessageDigest digest = null;
		String hashString;

		try {
			digest = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		digest.update(originalString.getBytes());
		hashString = DatatypeConverter.printHexBinary(digest.digest())
				.toLowerCase();
		return hashString;
	}

}
