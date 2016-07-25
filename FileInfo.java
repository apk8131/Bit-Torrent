/**
 *   BitTorrent : simulation of bitTorrent protocol
 *   
 *   @author Ashutosh Katkar
 *           Mansi Nahar
 *           
 *    FileInfo.java : helper class for client.java to create torrent file          
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.bind.DatatypeConverter;


public class FileInfo {
	String fileName;
	int noOfPieces;
	int pieceSize;
	int fileLength;
	String fileHash;
	
	/**
	 * default constructor
	 * @param fileName
	 * @param pieceSize
	 * @param fileLength
	 */
	public FileInfo(String fileName, int pieceSize,
			int fileLength) {
		this.fileName = fileName;
		this.noOfPieces = fileLength/pieceSize;
		this.pieceSize = pieceSize;
		this.fileLength = fileLength;
	}
	
	/**
	 *  getFileHash 
	 * @return SH1 hash value
	 */
	public String getFileHash() {
		System.out.println("Computing file hash!");
		int position = 0;
		MessageDigest digest = null;
		String hashString;
		
		try {
			digest = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} 
		
		for(int i = 0; i < noOfPieces; ++i) {
			byte [] piece = readPieceFromFile(fileName, position, (fileLength - (i*pieceSize)));
				if(piece != null) {
					digest.update(piece, 0, piece.length);
				}
		}
		
		hashString = DatatypeConverter.printHexBinary(digest.digest()).toLowerCase();
		return hashString;
	}
	
	/**
	 * readPieceFromFile
	 * @param fileName
	 * @param position
	 * @param size
	 * @return read content in bytes
	 */
	public byte[] readPieceFromFile(String fileName, int position, int size) {
		RandomAccessFile aFile;
		byte[] bytes = null;
		
		try {
			aFile = new RandomAccessFile(fileName, "r");
			aFile.seek(position);
			bytes = new byte[size];
			aFile.read(bytes);
			aFile.close();
		} 
		
		catch (FileNotFoundException e) {
			System.out.println(fileName + " doesn't seem to exist!");
			e.printStackTrace();
		}
		
		catch (IOException e) {
			System.out.println("Problem reading the file " +  fileName);
			e.printStackTrace();
		}
		return bytes;
	}
	
}
