/**
 *   BitTorrent : simulation of bitTorrent protocol
 *   
 *   @author Ashutosh Katkar
 *           Mansi Nahar
 *           
 *    MainPeer.java : Front end application using Swing            
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class MainPeer {

	static HashMap<String, TorrentInfo> allMyTorrents = new HashMap<String, TorrentInfo>();
	static HashMap<String, JProgressBar[]> progressbars = new HashMap<String, JProgressBar[]>();
	static HashMap<String, JLabel> displays = new HashMap<String, JLabel>();

	JFrame frame = new JFrame("MyTorrent");
	JPanel panel1 = new JPanel();
	static JPanel panel2 = new JPanel();
	JPanel panel3 = new JPanel();
	JButton createTorrent = new JButton("create torrent");
	JButton downloadFile = new JButton("Download file");

	/**
	 * Default constructor : initialise all swing components
	 */
	public MainPeer() {
		frame.setSize(1200, 800);
		frame.setMinimumSize(new Dimension(800, 600));
		frame.setLayout(new BorderLayout(20, 20));

		createTorrent.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Client c = new Client();

				JFileChooser chooser = new JFileChooser();
				int choice = chooser.showOpenDialog(chooser);
				if (choice != JFileChooser.APPROVE_OPTION)
					return;
				File f = chooser.getSelectedFile();
				// IP and port of tracker
				c.publishNewFile(f.getName(), "129.21.37.16", 64999);

			}
		});
		panel1.setLayout(new BorderLayout(20, 20));
		panel1.add(createTorrent, BorderLayout.WEST);

		panel3.setLayout(new BorderLayout(20, 20));

		downloadFile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				JFileChooser chooser = new JFileChooser();
				int choice = chooser.showOpenDialog(chooser);
				if (choice != JFileChooser.APPROVE_OPTION)
					return;
				File file = chooser.getSelectedFile();

				TorrentInfo torrentInfo = parseTorrent(file.getName());
				torrentInfo.printInfo();
				allMyTorrents.put(torrentInfo.fileInfo.fileName, torrentInfo);

				JProgressBar progressBar[] = new JProgressBar[torrentInfo.fileInfo.noOfPieces];

				progressbars.put(torrentInfo.fileInfo.fileName, progressBar);

				for (int i = 0; i < torrentInfo.fileInfo.noOfPieces; i++) {
					progressBar[i] = new JProgressBar();
					progressBar[i].setForeground(Color.GREEN);
					progressBar[i].setSize(10, 10);
					panel2.add(progressBar[i]);
				}

				JLabel display = new JLabel("downloading "
						+ torrentInfo.fileInfo.fileName);
				displays.put(torrentInfo.fileInfo.fileName, display);
				panel2.add(display);
				panel2.revalidate();
				panel2.repaint();
				PieceRequester pieceRequester = new PieceRequester(torrentInfo);
				pieceRequester.start();
			}
		});

		panel3.add(downloadFile, BorderLayout.WEST);

		frame.add(panel1, BorderLayout.NORTH);
		frame.add(panel2, BorderLayout.CENTER);
		frame.add(panel3, BorderLayout.SOUTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	/**
	 *  parseTOrrent : read torrent file and store data in TorrentInfo object
	 * @param fileName
	 * @return TorrentInfo 
	 */
	public TorrentInfo parseTorrent(String fileName) {
		TorrentInfo torrentInfo = null;
		try {

			FileReader fileReader = new FileReader(fileName);
			BufferedReader reader = new BufferedReader(fileReader);
			String line, trackerIP, infoString;
			int trackerPort;
			String torrentPath = fileName;

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

	public static void main(String[] args) {
		MainPeer mp = new MainPeer();

		PeerListener listener = new PeerListener(6500);
		listener.start();

	}

}
