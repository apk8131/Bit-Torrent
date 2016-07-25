/**
 *   BitTorrent : simulation of bitTorrent protocol
 *   
 *   @author Ashutosh Katkar
 *           Mansi Nahar
 *           
 *    RequesterThread.java : chunk requester            
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import javax.swing.JProgressBar;

public class RequesterThread extends Thread {

	Socket s;
	BufferedReader br;
	PrintWriter pw;
	File f;
	ArrayList<String> threadCreated = new ArrayList<String>();
	TorrentInfo torrentInfo;
	boolean isPeer;
	String requestedChunk = "";
	int chunksize;
	int chunkoffset;
	int chunkNo;
	String otherPeerIP;
	ArrayList<Integer> Availablechunks = new ArrayList<Integer>();

	/**
	 * Default constructor
	 * 
	 * @param IP
	 * @param port
	 * @param isPeer
	 * @param torrentInfo
	 * @param threadCreated
	 */
	public RequesterThread(String IP, int port, boolean isPeer,
			TorrentInfo torrentInfo, ArrayList<String> threadCreated) {
		try {
			s = new Socket(IP, port);

			this.otherPeerIP = IP;
			this.threadCreated = threadCreated;
			this.torrentInfo = torrentInfo;
			this.isPeer = isPeer;
			f = new File(torrentInfo.fileInfo.fileName);
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			pw = new PrintWriter(s.getOutputStream(), true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			if (torrentInfo.piecesNeeded.get(chunkNo) != null) {
				torrentInfo.piecesNeeded.remove(chunkNo);
				torrentInfo.piecesNeeded.put(chunkNo, false);
			}

		}
	}

	/**
	 * getChunkInfo
	 * 
	 * @throws IOException
	 */
	public void getChunkInfo() throws IOException {
		String line = "";
		while ((line = br.readLine()) != null) {
			if (line.contains("EndOfInfo"))
				break;
			Availablechunks.add(Integer.parseInt(line));
		}
	}

	/**
	 * parseRequestedChunk : store chunkSize, chunkNumber and chunkoffset
	 */
	public void parseRequestedChunk() {
		String[] token = requestedChunk.split(" ");
		chunksize = Integer.parseInt(token[0]);
		chunkNo = Integer.parseInt(token[1]);
		chunkoffset = Integer.parseInt(token[2]);
	}

	/**
	 * storeChunk : write received chunk to file
	 * 
	 * @throws IOException
	 */
	public void storeChunk() throws IOException {
		String data = "";
		int myChar;
		while ((myChar = br.read()) != -1) {
			if (data.contains("EndOfChunk"))
				break;
			data = data + (char) myChar;
		}

		RandomAccessFile raf = new RandomAccessFile(f, "rw");
		data = data.substring(0, data.indexOf("EndOfChunk"));

		if (chunkNo == torrentInfo.fileInfo.noOfPieces) {
			data.substring(0, data.length() - 2);
		}

		raf.seek(chunkoffset);
		raf.writeBytes(data);
		raf.close();

		// update at front end progress bar for that chunk
		JProgressBar chunkPro = MainPeer.progressbars
				.get(torrentInfo.fileInfo.fileName)[chunkNo - 1];
		chunkPro.setValue(100);

		synchronized (torrentInfo) {
			torrentInfo.havePieces.add(chunkNo);
			torrentInfo.piecesNeeded.remove(chunkNo);
			writeInFile(torrentInfo.fileInfo.fileName, chunkNo + " ");
		}
	}

	/**
	 * writeInFile : update meta file when chunk is received
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

	/**
	 * Algorithm which selected which is the next chunk to request from peers
	 * getNextPeice
	 * 
	 * @param piecesWithPeer
	 * @return chunkNumber
	 */
	public String getNextPiece(ArrayList<Integer> piecesWithPeer) {
		int requestPiece = 0;

		for (Integer pieceNumber : piecesWithPeer) {
			synchronized (torrentInfo) {
				if (torrentInfo.piecesNeeded.get(pieceNumber) != null) {
					if (!torrentInfo.piecesNeeded.get(pieceNumber)) {
						requestPiece = pieceNumber;
						torrentInfo.piecesNeeded.remove(pieceNumber);
						torrentInfo.piecesNeeded.put(pieceNumber, true);
						break;
					}
				}
			}
		}

		if (requestPiece == 0) {
			return "NONE";
		}
		return getPieceDetail(requestPiece);
	}

	/**
	 * Algorithm which selected which is the next chunk to request from seeder
	 * getNextPeice
	 * 
	 * @return chunkNumber
	 */
	public String getNextPiece() {
		int requestPiece = 0;
		int noOfPieces = torrentInfo.fileInfo.noOfPieces;

		for (int i = 1; i <= noOfPieces; ++i) {
			synchronized (torrentInfo) {
				if (torrentInfo.piecesNeeded.get(i) != null) {
					if (!torrentInfo.piecesNeeded.get(i)) {
						requestPiece = i;
						torrentInfo.piecesNeeded.remove(i);
						torrentInfo.piecesNeeded.put(i, true);
						break;
					}
				}
			}
		}

		if (requestPiece == 0) {
			return "NONE";
		}

		return getPieceDetail(requestPiece);
	}

	/**
	 * getPieceDetail
	 * 
	 * @param pieceNumber
	 * @return chunkSize + chunkNumber + chunk offset as string
	 */
	public String getPieceDetail(int pieceNumber) {
		String requestPieceString;

		int pieceSize = torrentInfo.fileInfo.pieceSize;
		int position = (pieceNumber - 1) * pieceSize;

		if (pieceNumber == torrentInfo.fileInfo.noOfPieces) {
			pieceSize = torrentInfo.fileInfo.fileLength - position;
		}

		requestPieceString = pieceSize + " " + pieceNumber + " " + position;

		return requestPieceString;
	}

	public void run() {

		while (!torrentInfo.piecesNeeded.isEmpty()) {
			String cmd = "";
			try {

				if (isPeer) {
					pw.println("sendInfo " + torrentInfo.fileInfo.fileName);
					cmd = br.readLine();
					if (cmd.contains("responseInfo")) {
						getChunkInfo();
					}

					// call algorithm ( availableChunks)
					requestedChunk = getNextPiece(Availablechunks);

					// if its seeders
				} else {
					// call algorithm () for seeder
					requestedChunk = getNextPiece();
				}

				if (!requestedChunk.contains("NONE")) {

					parseRequestedChunk();

					pw.println("sendChunk " + torrentInfo.fileInfo.fileName
							+ " " + requestedChunk);
					cmd = br.readLine();

					if (cmd.contains("sendingChunk")) {
						storeChunk();
					}
				}
			} catch (Exception e) {
				// failure handling
				if (threadCreated.indexOf(otherPeerIP) != -1) {
					threadCreated.remove(threadCreated.indexOf(otherPeerIP));
				}
				synchronized (torrentInfo) {
					if (torrentInfo.piecesNeeded.get(chunkNo) != null) {
						torrentInfo.piecesNeeded.remove(chunkNo);
						torrentInfo.piecesNeeded.put(chunkNo, false);
					}
				}
			}

		}
	}

}
