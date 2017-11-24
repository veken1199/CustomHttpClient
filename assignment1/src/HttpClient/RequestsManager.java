package HttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

public class RequestsManager {
	
	private Socket socket;
	private InetAddress address;
	private PrintWriter out;
	private BufferedReader reader;
	private StreamManager stream_manager;
	public  Utilities utilities = new Utilities();
	
	// Sending GET request
	public String GET(String _url, String header, boolean verbose){
		
		String complete_response = "Response did not process";
		
		// Get the url and sub directories
		String[] url = this.utilities.parseURL(_url);
		String url_parsed = url[0];
		String directory_parsed = url[1];
		String redirect = "";
		
		Map<String, String> headers_paresed = this.utilities.mapConverter(header);
		
		//saasd 
		if(verbose){
			this.requestDisplay(url_parsed, directory_parsed, "GET", headers_paresed);
		}
		
		try{
			this.prepareRequest(url_parsed);
			
			// Pass the headers to the server
			this.out.println("GET " + directory_parsed + " HTTP/1.1");
			this.out.println("Host: " + url_parsed);
			
		   
			// Pass the requested headers
			for (Map.Entry<String, String> entry : headers_paresed.entrySet()){
				this.out.println(entry.getKey() + ": " + entry.getValue()); 
			} 
		   
			this.out.println();
			this.out.flush();
			
			
			// Read the reponse from the server
			complete_response = stream_manager.readResponse(reader, verbose);
			redirect = this.redirect((stream_manager.header_val));
			
			if(!redirect.equals("")){
				// If we get here then that means there is redirection 
				// print the original response
								
				System.out.println(complete_response);
				System.out.println("");
								
				//let the user know redirection is in course
				System.out.println("You are being redirected to " + redirect + " . . . \n");
								
				complete_response = this.GET(redirect, header, verbose);
			}
					
			
			socket.close();
			this.out.close();
			this.reader.close();
			
			
		}
		
		catch(Exception e){
			System.out.print(url_parsed + " " + e.getMessage());
		}
		
		return complete_response;
	}
	
	
	// Sending POST request
	public String POST(String _url, String header, boolean verbose, String data){
		
		// Parsing the request info using utilities class
		String complete_response = "Response did not process";
		String[] url= this.utilities.parseURL(_url);
		String url_parsed = url[0];
		String directory_parsed = url[1];
		String redirect = "";
		Map<String, String> headers_parsed = this.utilities.mapConverter(header);
		
		if(verbose){
			this.requestDisplay(url_parsed, directory_parsed, "POST", headers_parsed);
		}
		
		try{
			this.prepareRequest(url_parsed);
			
			// Pass the headers to the server
			this.out.println("POST " + directory_parsed + " HTTP/1.1");
			this.out.println("Host: " + url_parsed);
			this.out.println("Content-Length: " + data.length());
			
					
			// Pass the requested headers
			for (Map.Entry<String, String> entry : headers_parsed.entrySet()){
				this.out.println(entry.getKey() + ": " + entry.getValue()); 
			} 
				
			this.out.println();
			this.out.println(data);
			this.out.flush();
					
			// Read the reponse from the server
			complete_response = stream_manager.readResponse(reader, verbose);
			redirect = this.redirect(stream_manager.header_val);
		
			if(! redirect.equals("")){
				// If we get here then that means there is redirection 
				// print the original response
				
				System.out.println(complete_response);
				System.out.println("");
				
				//let the user know redirection is in course
				System.out.println("You are being redirected . . . \n");
				
				complete_response = this.POST(redirect, header, verbose, data);
			}
		}
		
		catch(Exception e){
			System.out.println(e.getLocalizedMessage());
		}
		
		return complete_response;
	}
	
	
	// Method responsible for opening sockets and preparing input/output streams
	private void prepareRequest(String url) throws Exception{
		this.address = InetAddress.getLocalHost();
		this.socket  = new Socket(address, 50);
		this.out = new PrintWriter(this.socket.getOutputStream());
		this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		this.stream_manager = new StreamManager();
	}
	
	// Method responsible for displaying the request info
	private void requestDisplay(String url, String sublink, String method, Map<String, String> headers){
		System.out.println("---------------Request information---------------");
		System.out.println("Processing: " + url);
		System.out.println("To: " + sublink);
		System.out.println("Method: " + method);
		System.out.println("Headers: ");
		
		for (Map.Entry<String, String> entry : headers.entrySet()){
			System.out.println(entry.getKey() + ":" + entry.getValue() );
		}
		
		System.out.println("---------------End of request information---------------");
	}
	
	public String redirect(String header){
		String redirect = "";
		if (header.contains("HTTP/1.1 302")){
			redirect = header.substring(header.indexOf("Location")+9, header.indexOf("\n", header.indexOf("Location")));
			redirect = redirect.trim();
		}
		return redirect;
	}

}
