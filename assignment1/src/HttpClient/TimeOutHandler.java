package HttpClient;

import javax.management.RuntimeErrorException;


import java.lang.Thread.UncaughtExceptionHandler;

public class TimeOutHandler implements UncaughtExceptionHandler{
	
	public UDPRequestManager s;
	private String msg;
	private String type;
	
	public void uncaughtException(Thread th, Throwable ex){
		switch(type){
		case "handshake":
			if(s.count < 4){
				System.out.println("Try handshake again " + s.count);
				s.retransmitHandshake();
				break;
			}
			else{
				s.connectionState = "Bad";
				System.out.println("The connection is closed");
			}
			
		
		case "resendGetRequest":
			if(!s.connectionState.equals("DONE")){
				s.retransmitRequest();
				break;
			}
			
		case "sendpackets":
			s.senderRetransmitWindow();
			break;
		}
	}
	
	public TimeOutHandler(UDPRequestManager client, String errorMsg, String type) {
		this.s = client;
		this.msg = errorMsg;
		this.type = type;
	}
					
}
