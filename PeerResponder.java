/**
 *   BitTorrent : simulation of bitTorrent protocol
 *   
 *   @author Ashutosh Katkar
 *           Mansi Nahar
 *           
 *    PeerResponder.java : handles the request from each peer            
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;

public class PeerResponder extends Thread {

	Socket s;
	BufferedReader br;
	PrintWriter pw;
	File f;

	/**
	 * Default constructor
	 * 
	 * @param s
	 * @throws IOException
	 */
	public PeerResponder(Socket s) throws IOException {
		this.s = s;
		br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		pw = new PrintWriter(s.getOutputStream(), true);
	}

	/**
	 * init
	 * 
	 * @param filename
	 */
	public void init(String filename) {
		f = new File(filename);
		pw.println("responseInfo");
		TorrentInfo torrentInfo = Peer.allMyTorrents.get(filename);
		// send available chunk numbers to peer
		for (int i = 0; i < torrentInfo.havePieces.size(); i++) {
			pw.println(torrentInfo.havePieces.get(i));
		}
		pw.println("EndOfInfo");
	}

	/**
	 * sendFile
	 * 
	 * @param chunkNumber
	 * @param chunkSize
	 * @param offset
	 * @throws IOException
	 */
	public void sendFile(int chunkNumber, int chunkSize, int offset)
			throws IOException {
		RandomAccessFile raf = new RandomAccessFile(f, "r");
		byte[] data = new byte[chunkSize];
		raf.seek(offset);
		raf.read(data);
		raf.close();
		pw.println("sendingChunk");
		pw.print(new String(data));
		pw.println("EndOfChunk");
		System.out.println("chunksent " + chunkNumber);
	}

	public void run() {
		while (true) {
			try {

				String line;
				String cmd = null;

				while ((line = br.readLine()) != null) {
					cmd = line;
					break;
				}

				if (cmd.contains("sendInfo")) {
					String[] token = cmd.split(" ");
					String filename = token[1];
					init(filename);
				}

				if (cmd.contains("sendChunk")) {
					String[] token = cmd.split(" ");
					f = new File(token[1]);
					int chunkSize = Integer.parseInt(token[2]);
					int chunkNumber = Integer.parseInt(token[3]);
					int offset = Integer.parseInt(token[4]);
					sendFile(chunkNumber, chunkSize, offset);
				}

			} catch (Exception e) {
				// if connect lost thread stops 
				break;
			}
		}
	}

}
