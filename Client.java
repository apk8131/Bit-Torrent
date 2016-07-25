/**
 *   BitTorrent : simulation of bitTorrent protocol
 *   
 *   @author Ashutosh Katkar
 *           Mansi Nahar
 *           
 *    Client.java : seeder as client create torrent file and inform tracker              
 */

import java.io.File;
import java.util.ArrayList;

public class Client {

	/**
	 * publishFile
	 * 
	 * @param filePath
	 * @param trackerIP
	 * @param trackerPort
	 */
	public void publishNewFile(String filePath, String trackerIP,
			int trackerPort) {
		File file = new File(filePath);
		int pieceSize = 1024;
		ArrayList<FileInfo> allFileInfo = new ArrayList<FileInfo>();
		String torrentName;

		// multiple file torrent
		if (file.isDirectory()) {
			torrentName = file.getName();
			String[] files = file.list();
			for (int i = 0; i < files.length; ++i) {
				File aFile = new File(files[i]);
				FileInfo fileInfo = new FileInfo(files[i], pieceSize,
						(int) aFile.length());
				fileInfo.fileHash = fileInfo.getFileHash();
				allFileInfo.add(fileInfo);
			}
		}
		// single file torrent
		else {
			torrentName = file.getName().substring(0,
					file.getName().indexOf('.'));
			FileInfo fileInfo = new FileInfo(filePath, pieceSize,
					(int) file.length());
			System.out.println("Calling fileHash method");
			fileInfo.fileHash = fileInfo.getFileHash();
			allFileInfo.add(fileInfo);
		}

		TorrentCreator creator = new TorrentCreator();
		creator.createTorrentFile(torrentName, allFileInfo, trackerIP,
				trackerPort);
		creator.infomTracker(allFileInfo, trackerIP, trackerPort);

	}

	public static void main(String args[]) {
		Client client = new Client();
		client.publishNewFile(args[0], args[1], Integer.parseInt(args[2]));
	}
}
