package HttpClient;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;

public class UDPRequestManager extends RequestsManager {
	public static SocketAddress ROUTERADDRESS;
	public static InetSocketAddress SERVERADDRESS; //assumption that the server is always on the localhost
	public DatagramChannel UDPChannel;
	
	public UDPRequestManager(){
		this.SERVERADDRESS = new InetSocketAddress("localhost", 8007);
		this.ROUTERADDRESS = new InetSocketAddress("localhost", 3000);
	}
	
	public String GET(String _url, String header, boolean verbose){
		try(DatagramChannel channel = DatagramChannel.open()){
            String msg = "Hello World";
            Packet p = new Packet.Builder()
                    .setType(0)
                    .setSequenceNumber(1L)
                    .setPortNumber(this.SERVERADDRESS.getPort())
                    .setPeerAddress(this.SERVERADDRESS.getAddress())
                    .setPayload(msg.getBytes())
                    .create();
            channel.send(p.toBuffer(), ROUTERADDRESS);
		}
		catch(Exception e){
			System.out.println("ERROR202: the packet cannot be sent!");
		}
		
		return "sent";    
	}
	
	public String POST(String _url, String header, boolean verbose){
		
		return null;
	}
	
	private boolean threeWayHandShake(){
		
		return true;
	}
	
	
}
