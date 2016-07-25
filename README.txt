/**
 *   BitTorrent : simulation of bitTorrent protocol
 *   
 *   @author Ashutosh Katkar
 *           Mansi Nahar
 *                     
 */
 
  In bit Torrent, there is tracker which is always on which act as server to get information about peers and seeders.
  
  1. when Peer create a torrent file, it inform tracker, tracker store that peer as seeder
  2. When any peer need to download file, firstly it should have .torrent file. Peer read it and get tracker info
     and file Info. Then it contact tracker and get peer and seeder info for that file.
  3. Peer then contact each peer and ask for available pieces, then algorithm decide which chunk number to request.
     As seeder has all chunks, algorithm directly ask next required chunk from seeder.
  4. Once peer received all chunks, it become seeder and update it to tracker.
  5. Peer after every 10 seconds ask tracker if there is any new peer or seeder available.
  
  System information:
   1. for front end version of peer, tracker IP and port is hard coded. You can change it as per requirement.
   2. All peers are listening requests on port 6500. You must start all peers at this port only.
   3. Current version work for separate torrent file for each file.
   4. Peer can download more than one files simultaneously.
   5. If any seeder/peer is down, download stops, it resume as soon as its up.
   6. Tracker is always on.      
        
   Execution steps:
   1. Compile all files.
   
   2. Start tracker by 
      
      java Tracker <port>
      
      if you want to run front end ( change IP and port of tracker in MainPeer.java)
   
   3. Create torrent file
   
      java Client <file> <Tracker_IP> <Tracker_Port>    
   
      if you are using front end, 
   
      Java MainPeer
   
      click on create torrent button. File explorer will open. select file.
      It will create filename.torrent file.
   
   4. (when ever peer want to download file it should have .torrent file of that file. 
      For simulation we keep it on common file system)
      Start peer
      If its seeder just start it.
       
      Java Peer 6500         
       
      If its peer and want to download file.
       
      Java Peer 6500 <torrent_file> 
       
      If you are using front end, 
      click on download file button. File explorer will open. select .torrent file.
       