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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.management.RuntimeErrorException;


public class UDPRequestManager extends RequestsManager {

	public static SocketAddress ROUTERADDRESS;
	public static InetSocketAddress SERVERADDRESS; // assumption that the server

	public String connectionState;
	private ByteBuffer buf;
	public DatagramChannel UDPChannel;
	private WindowManager windowManager;
	private CustomTimeout timer;
	private Packet requestPacket;
	private Thread.UncaughtExceptionHandler h;
	public int count = 0;

	// Constructor
	public UDPRequestManager() {
		this.SERVERADDRESS = new InetSocketAddress("localhost", 8007);
		this.ROUTERADDRESS = new InetSocketAddress("localhost", 3000);
		this.windowManager = new WindowManager();
		buf = ByteBuffer.allocate(Packet.MAX_LEN);
		this.connectionState = "NONE";
		this.timer = new CustomTimeout(1);
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

				this.requestPacket = this.packetBuilder(8, this.prepareGET(directory, headers),
						503);
				
				this.sendPacket(this.requestPacket);

				System.out.println("Request is sent... waiting for confirmation");
				
				this.setUpTimer("Retransmitting the request", "resendGetRequest", (long)(2));
			
				this.connectionState = "REQ_SENT";
			}

			// ack with type 9 prepare the window
			if (this.connectionState.equals("REQ_SENT")) {
				
				buf = ByteBuffer.allocate(Packet.MAX_LEN).order(ByteOrder.BIG_ENDIAN);
				SocketAddress router = UDPChannel.receive(buf);
				buf.flip();
				Packet packet = Packet.fromBuffer(buf);
					
				if (packet.getType() == 5) {
					
					this.connectionState = "DATA";
					int numberOfPackets = Integer.parseInt(new String(packet.getPayload()));
					windowManager.setWINDOWSIZE(5);
					windowManager.setCurrentSequenceNumber(1);
					windowManager.initializeReceiverWindow(1, Integer.parseInt(new String(packet.getPayload())));

					windowManager.initialShiftWindow();

					// setting up the sliding window system
					windowManager.setPacketsNumber(Integer.parseInt(new String(packet.getPayload())));
					this.setUpTimer("Retransmitting the window confirmation", "resendGetRequest", (long)(3));
					this.requestPacket = this.packetBuilder(9, "", packet.getSequenceNumber()+1);
					
					this.sendPacket(this.requestPacket);
					
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
					
					
					if(packet2.getType() != 7)
					{
					
					}
					
					// step 1 check if the current packet sequence number is
					// within the window
					else if (windowManager.isPacketAccepted(packet2)) {
						this.connectionState = "DATA_RES";
						System.out.println("processing packet sn: " + packet2.getSequenceNumber());
						// remove the packet sequence number
						windowManager.removePacketFromReceiverWindow(packet2);

						// add the packet to receivedPackets
						windowManager.insertPacketReceivedPackets(packet2);

						// send ack
						this.sendPacket(this.packetBuilder(10, "", packet2.getSequenceNumber()));

					} else {
						System.out.println("Packet " + packet2.getSequenceNumber() + " ALREADY ACKED");
						this.sendPacket(this.packetBuilder(10, "", packet2.getSequenceNumber()));
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
				this.requestPacket = this.packetBuilder(5, numberOfPackets + "", 503);
				this.sendPacket(this.requestPacket);
				
				this.windowManager.initializeSenderWindow(1, numberOfPackets);
				windowManager.initialShiftWindow();
				System.out.println("\n---------------------------------");
				System.out.println("Request is sent... waiting for confirmation");
				this.setUpTimer("Retransmitting the request", "resendGetRequest", (long)(2));
				this.connectionState = "SYN_SIZE";
			}

			while (!windowManager.isSenderTransmissionDone()) {
				
				buf = ByteBuffer.allocate(Packet.MAX_LEN).order(ByteOrder.BIG_ENDIAN);
				SocketAddress router = UDPChannel.receive(buf);
				buf.flip();
				Packet pack = Packet.fromBuffer(buf);
				
				if (pack.getType() == 9) {
					this.setUpTimer("Retransmitting the window packets", "sendpackets", (long)(2) );
					this.connectionState = "DATA_SEND";
					windowManager.sendWindow();
					continue;
				}

				if (pack.getType() == 10) {
					this.connectionState = "DATA_SEND";
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
						this.connectionState = "DONE";
						System.out.println("The request is complete!");
						break;
					}
				}
			}

			// waiting for receiver's response, 1 packet
			buf = ByteBuffer.allocate(Packet.MAX_LEN).order(ByteOrder.BIG_ENDIAN);
			boolean e = false;
			boolean res = false;
			while(!e){
				
			SocketAddress router = UDPChannel.receive(buf);
			buf.flip();
			Packet pack = Packet.fromBuffer(buf);
			
			if(pack.getType() == 12 && !res ){
				System.out.println("----------------------------------");
				System.out.println(new String(pack.getPayload()));
				System.out.println("----------------------------------");
				res = true;
				// send the last ack
				windowManager.sendPacket(this.packetBuilder(10, "", -100));
			}
			if (pack.getType() == 10 && pack.getSequenceNumber() == -100) {
				System.out.println("Connection is closed");
				e = true;
			}
		}

		}

		catch (Exception e) {
			System.out.println(e.getStackTrace());
		}
		return data;
	}

	public void threeWayHandShake() throws Exception {

		Thread.UncaughtExceptionHandler h = new TimeOutHandler(this, "Handshake Has timed out", "handshake");

		this.timer = new CustomTimeout(4L);
		Thread timeWorker = new Thread(timer);
		timeWorker.setUncaughtExceptionHandler(h);
		timeWorker.start();
		
		while (this.count<4) {
			if (this.connectionState.equals("NONE")) {
				// step 1, send SYN packet type
				
				System.out.println("Try " + count);
				Packet p = this.packetBuilder(0, "HI", 500);
				this.windowManager = new WindowManager(p, UDPChannel);
				this.sendPacket(p);

				this.connectionState = "SYN_SENT";
				System.out.println("Threeway handshake started : 1/3");
				this.requestPacket = p;
			}

			else if (this.connectionState.equals("SYN_SENT")) {
				// we need to wait for a response
				
				SocketAddress router = UDPChannel.receive(buf);
				buf.flip();
				Packet packet = Packet.fromBuffer(buf);
				// step2, send ACK packet type
				Packet p2 = this.packetBuilder(2, "Hello There!", 502);
				this.sendPacket(p2);
				this.sendPacket(p2);
				
				this.timer.stopTimer();
				
				if(this.connectionState.equals("SYN_SENT")){
					this.connectionState = "ESTABLISHED";
					System.out.println("Threeway handshake established : 3/3");
				}
			}

			else if (this.connectionState.equals("ESTABLISHED")) {
				break;
			}
			
			else if (this.connectionState.equals("Bad")){
				this.connectionState = "NONE";
				continue;
			}
			
			else {
				System.out.println("ERROR231: Error occured while checking the states of the connection!");
			}
		}
	}

	public Packet packetBuilder(int type, String payload, long sequenceNumber) {
		return new Packet.Builder().setType(type).setSequenceNumber(sequenceNumber)
				.setPortNumber(this.SERVERADDRESS.getPort()).setPeerAddress(this.SERVERADDRESS.getAddress())
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

	public String preparePOST(String directory, Map<String, String> headers, String data) {
		return "POST " + directory + " HTTP/1.1\n" + "Host: " + this.SERVERADDRESS + "\n" + "content-length: "
				+ data.length() + "\n\n";
	}

	public void retransmitRequest() {
		if(this.connectionState.equals("SYN_SIZE") || this.connectionState.equals("REQ_SENT") ||this.connectionState.equals("REQ_ESTABLISHED") ){
			System.out.println("#Retransmitting Request ....");
			this.sendPacket(this.requestPacket);
			this.setUpTimer("Retransmitting the request", "resendGetRequest", (long)(2));
		}	
	}
	
	public void setUpTimer(String msg, String type, long duration){
		this.h = new TimeOutHandler(this, msg, type);
		this.timer = new CustomTimeout(duration);
		Thread timeWorker = new Thread(timer);
		timeWorker.setUncaughtExceptionHandler(h);
		timeWorker.start();
	}
	
	public void senderRetransmitWindow(){
		if(!this.windowManager.isWindowReceived()){
			System.out.println("herereere");
			this.windowManager.senderRetransmitWindow();
			this.setUpTimer("Retransmitting the window packets", "sendpackets", (long)(2) );
		}
	}
	
	public void retransmitHandshake(){
		if(this.connectionState == "SYN_SENT"){
			this.count++;
			this.setUpTimer("Retransmitting the handshake packet packets","handshake", (long)(1) );
			sendPacket(this.requestPacket);
			
		}
	}
}
