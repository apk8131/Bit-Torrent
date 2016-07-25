/**
 *   BitTorrent : simulation of bitTorrent protocol
 *   
 *   @author Ashutosh Katkar
 *           Mansi Nahar
 *           
 *    TorrentInfo.java : helper class which contain all torrent file info          
 */

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.bind.DatatypeConverter;


public class TorrentInfo {
	String trackerIP;
	int trackerPort;
	String infoString;
	String infoHash;
	FileInfo fileInfo;
	String torrentPath;
	HashMap<String, Integer> seeders;
	HashMap<String, Integer> peers;
	HashMap<Integer, Boolean> piecesNeeded = new HashMap<Integer, Boolean>();
	ArrayList<Integer> havePieces = new ArrayList<Integer>();
	
	/**
	 * Default constructor
	 * @param trackerIP
	 * @param trackerPort
	 * @param infoString
	 * @param fileInfo
	 * @param torrentPath
	 */
	public TorrentInfo(String trackerIP, int trackerPort, String infoString, FileInfo fileInfo, String torrentPath) {
		super();
		this.torrentPath = torrentPath;
		this.trackerIP = trackerIP;
		this.trackerPort = trackerPort;
		this.infoString = infoString;
		this.infoHash = getHash(infoString);
		this.fileInfo = fileInfo;
		seeders = new HashMap<String, Integer>();
		peers = new HashMap<String, Integer>();
		
		for( int i = 0; i < fileInfo.noOfPieces; ++i) {
			piecesNeeded.put(i+1, false);
		}
	}
	
	/**
	 *  getHash
	 * @param originalString
	 * @return SH1 hash value
	 */
	String getHash(String originalString){
		
		MessageDigest digest = null;
		String hashString;
		
		try {
			digest = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} 
		
		 digest.update(originalString.getBytes());
		 hashString = DatatypeConverter.printHexBinary(digest.digest()).toLowerCase();
		 return hashString;
	}
	
	/**
	 * print all info about tracker, peers and seeders
	 */
	public void printInfo() {
		System.out.println("Tracker IP: " + trackerIP);
		System.out.println("Tracker Port: " + trackerPort);
		System.out.println("Info String: " + infoString);
		System.out.println("Info Hash: " + infoHash);
		
		System.out.println("Peers");
		Iterator<Entry<String, Integer>> peerIterator = peers.entrySet().iterator();
		while(peerIterator.hasNext()) {
			Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>)peerIterator.next();
			System.out.println("Peer IP: " + pair.getKey());
			System.out.println("Peer port: " + pair.getValue());
		}
		
		System.out.println("Seeders");
		Iterator<Entry<String, Integer>> SeederIterator = seeders.entrySet().iterator();
		while(SeederIterator.hasNext()) {
			Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>)SeederIterator.next();
			System.out.println("Seeder IP: " + pair.getKey());
			System.out.println("Seeder port: " + pair.getValue());
		}
		
	}
}
