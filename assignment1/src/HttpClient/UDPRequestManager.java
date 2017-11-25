package HttpClient;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class UDPRequestManager extends RequestsManager {

	public static SocketAddress ROUTERADDRESS;
	public static InetSocketAddress SERVERADDRESS; // assumption that the server
													
	private String connectionState;
	private ByteBuffer buf;
	private DatagramChannel UDPChannel;
	private WindowManager windowManager;

	
	// Constructor
	public UDPRequestManager() {
		this.SERVERADDRESS = new InetSocketAddress("localhost", 8007);
		this.ROUTERADDRESS = new InetSocketAddress("localhost", 3000);
		this.windowManager = new WindowManager();
		buf = ByteBuffer.allocate(Packet.MAX_LEN);
		this.connectionState = "NONE";
	}

	
	// GET Method
	public String GET(String _url, String header, boolean verbose) {
		try {
			// We just want a single response.
			UDPChannel = DatagramChannel.open();

			if (!this.connectionState.equals("ESTABLISHED")) {
				this.threeWayHandShake();
			}

			if (this.connectionState.equals("ESTABLISHED")) {
				// Get the url and sub directories
				String[] url = this.utilities.parseURL(_url);
				String directory = url[1];
				Map<String, String> headers = this.utilities.mapConverter(header);

				// 8 for GET requests

				this.sendPacket(this.packetBuilder(8, this.prepareGET(directory, headers),
						windowManager.getPacketNumberAndIncreament()));

				System.out.println("Request is sent... waiting for confirmation");
				this.connectionState = "REQ_SENT";
			}

			// ack with type 9 prepare the window
			if (this.connectionState.equals("REQ_SENT")) {

				buf = ByteBuffer.allocate(Packet.MAX_LEN).order(ByteOrder.BIG_ENDIAN);
				SocketAddress router = UDPChannel.receive(buf);
				buf.flip();

				Packet packet = Packet.fromBuffer(buf);

				if(packet.getType() == 5){
					windowManager.setWINDOWSIZE(5);
					windowManager.setCurrentSequenceNumber(1);
					windowManager.initializeReceiverWindow(1, Integer.parseInt(new String(packet.getPayload())));

					windowManager.initialShiftWindow();

					// setting up the sliding window system
					windowManager.setPacketsNumber(Integer.parseInt(new String(packet.getPayload())));

					this.sendPacket(this.packetBuilder(9, "", windowManager.getCurrentSequenceNumber()));
					System.out.print("Window created, ready to receive packets");
					this.connectionState = "REQ_ESTABLISHED";
				}
			}

			if (this.connectionState.equals("REQ_ESTABLISHED")) {

				System.out.println("Size of the receiver window: " + windowManager.getPacketsNumber());

				// start getting the packets
				while (!(windowManager.isTransmisionComplete())) {
					// check if we can slide
					if (!windowManager.isWindowWaitingForPackets()) {
						windowManager.shiftWindow();
					}

					buf = ByteBuffer.allocate(Packet.MAX_LEN).order(ByteOrder.BIG_ENDIAN);

					SocketAddress router2 = UDPChannel.receive(buf);
					buf.flip();
					Packet packet2 = Packet.fromBuffer(buf);

					System.out.println("processing packet sn: " + packet2.getSequenceNumber());
					// step 1 check if the current packet sequence number is
					// within the window
					if (windowManager.isPacketAccepted(packet2)) {

						// remove the packet sequence number
						windowManager.removePacketFromReceiverWindow(packet2);

						// add the packet to receivedPackets
						windowManager.insertPacketReceivedPackets(packet2);

						// send ack
						this.sendPacket(this.packetBuilder(10, "", packet2.getSequenceNumber()));

					} else {
						System.out.print("Packet " + packet2.getSequenceNumber() + " REJECTED");
					}
				}

				System.out.println("Transction is done!");
				System.out.println("Constructing the reponse for the packets");
				System.out.println("------------------------------------------");
				System.out.println(windowManager.decodePackets());
				System.out.println("------------------------------------------");
			}
			// parse the get request into a packet
		}

		catch (Exception e) {
			System.out.println("ERROR202: the packet cannot be sent!" + e.fillInStackTrace());
		}

		return "";
	}

	public String POST(String _url, String header, boolean verbose, String data) {
		try {
			// We just want a single response.
			UDPChannel = DatagramChannel.open();
			
			if (!this.connectionState.equals("ESTABLISHED")) {
				this.threeWayHandShake();
			}

			if (this.connectionState.equals("ESTABLISHED")) {
				// Get the url and sub directories and create packets and sent
				// the server the number of packets

				String[] url = this.utilities.parseURL(_url);
				String directory = url[1];
				Map<String, String> headers = this.utilities.mapConverter(header);
				
				System.out.println("----------Window Construction-------------");
				byte[] requestBytes = (this.preparePOST(directory, headers, data) + data).getBytes();
				int numberOfPackets = (requestBytes.length / 1013) + 1;
				System.out.println("Preparing : " + numberOfPackets + " Packets");
				
				this.windowManager.setPacketsNumber(numberOfPackets);
				this.windowManager.windowPacketBuilder(numberOfPackets, requestBytes);

				// number send the number of the packet the sender will
				// be expecting
				// setting the state to SYN-SIZE
				windowManager.setCurrentSequenceNumber(1);
				windowManager.setWINDOWSIZE(5);
				this.sendPacket(this.packetBuilder(5, numberOfPackets + "" , 1));

				this.windowManager.initializeSenderWindow(1, numberOfPackets);
				windowManager.initialShiftWindow();
				System.out.println("\n---------------------------------");
				System.out.println("Request is sent... waiting for confirmation");
				this.connectionState = "SYN_SIZE";
			}
			
			while(!windowManager.isSenderTransmissionDone()){
				
				buf = ByteBuffer.allocate(Packet.MAX_LEN).order(ByteOrder.BIG_ENDIAN);
				SocketAddress router = UDPChannel.receive(buf);
				buf.flip();
				Packet pack = Packet.fromBuffer(buf);
				
				if (pack.getType() == 9) {
					windowManager.sendWindow();
					continue;
				}
				
				if (pack.getType() == 10) {
	
					System.out.println("Ack received with seq num : " + pack.getSequenceNumber());
					if (windowManager.isValidAct(pack)) {

						System.out.println("Removing the the acked packet, " + pack.getSequenceNumber());
						this.windowManager.receiveAck(pack);

						// check if we can shift the window now
						if (this.windowManager.isWindowReceived()) {
							this.windowManager.shiftWindow();
							this.windowManager.sendWindow();
						}
					} 
					
					else {
						System.out.println("Received a delayed ACK " + pack.getSequenceNumber());
					}

					// check if we transmitted all the files
					if (this.windowManager.isSenderTransmissionDone()) {
						System.out.println("The request is complete!");
						break;
					}
				}
			}
			
			//waiting for receiver's response, 1 packet
			buf = ByteBuffer.allocate(Packet.MAX_LEN).order(ByteOrder.BIG_ENDIAN);

			SocketAddress router = UDPChannel.receive(buf);
			buf.flip();
			Packet pack = Packet.fromBuffer(buf);
			
			System.out.println("----------------------------------");
			System.out.println(new String(pack.getPayload()));
			System.out.println("----------------------------------");
			
			//send the last ack
			windowManager.sendPacket(this.packetBuilder(10, "", -100));
			

			SocketAddress router2 = UDPChannel.receive(buf);
			buf.flip();
			pack = Packet.fromBuffer(buf);
			
			if(pack.getType() == 10 && pack.getSequenceNumber() == -100){
				System.out.println("Connection is closed");
			}
			
		}

		catch (Exception e) {
			System.out.println(e.getStackTrace());
		}
		return data;
	}

	
	private void threeWayHandShake() throws IOException {
		while (true) {
			if (this.connectionState.equals("NONE")) {
				// step 1, send SYN packet type
				Packet p = this.packetBuilder(0, "HI", 0);
				this.windowManager = new WindowManager(p, UDPChannel);
				this.sendPacket(p);

				this.connectionState = "SYN_SENT";
				System.out.println("Threeway handshake started : 1/3");
			}

			else if (this.connectionState.equals("SYN_SENT")) {
				// we need to wait for a response
				SocketAddress router = UDPChannel.receive(buf);
				buf.flip();
				Packet packet = Packet.fromBuffer(buf);

				// step2, send ACK packet type
				Packet p2 = this.packetBuilder(1, "Hello There!", 1);
				this.sendPacket(p2);

				this.connectionState = "ESTABLISHED";
				System.out.println("Threeway handshake established : 3/3");
			}

			else if (this.connectionState.equals("ESTABLISHED")) {
				break;
			}

			else {
				System.out.println("ERROR231: Error occured while checking the states of the connection!");
			}
		}
	}

	public Packet packetBuilder(int type, String payload, long sequenceNumber) {
		return new Packet.Builder().setType(type).setSequenceNumber(sequenceNumber)
				.setPortNumber(this.SERVERADDRESS.getPort())
				.setPeerAddress(this.SERVERADDRESS.getAddress())
				.setPayload(payload.getBytes()).create();
	}

	public void sendPacket(Packet packet) {
		try {
			this.UDPChannel.send(packet.toBuffer(), ROUTERADDRESS);
		}

		catch (IOException e) {
			System.out.println("ERROR444:The server could not sent the packet");
		}
	}

	public String prepareGET(String directory, Map<String, String> headers) {
		return "GET " + directory + " HTTP/1.1\n" + "Host: " + this.SERVERADDRESS + "\n";
	}
	
	public String preparePOST(String directory, Map<String, String> headers, String data){
		return "POST " + directory + " HTTP/1.1\n" + "Host: " + this.SERVERADDRESS +"\n" + "content-length: " + data.length()+"\n\n";
	}
}
