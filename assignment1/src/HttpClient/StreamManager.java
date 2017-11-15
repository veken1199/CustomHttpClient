package HttpClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class StreamManager {
	
	private BufferedReader reader;
	private String header_top = "---------------Response Header--------------- \n";
	private String header_buttom = "---------------End of Response Header--------------- \n";
	private String response_top = "---------------Response Body--------------- \n";
	private String response_buttom = "---------------End of Response Body---------------\n";
	public String header_val = "";

	
	// Method responsible for reading the data from a file and pass 
	// the content to the POST method, or any other entity requested it.
	public String readFile(String filename)throws Exception{
		String data = "";
		String partial_data = "";
		
		this.reader = new BufferedReader(new FileReader(filename));
		while((partial_data = reader.readLine()) != null){
			data = data + partial_data + "\n";
		}
		
		reader.close();
		return data;
	}
	
	// Method responsiblt for reading and returning the response from the server.
	public String readResponse(BufferedReader reader, boolean verbose)throws Exception{
		String complete_response = "";
		String input_line = "";
		String header_response = "";
		boolean loop = true;	
		
		while (loop) {
			if (reader.ready()) {
				while ((input_line = reader.readLine()) != null) {
			        // finding the header in the reponse
			        // if the part of the stream is empty and the 'header' variable is empty,
			        // the part of the stream before the the response is the header
			        if(input_line.equals("") && header_response.equals("")){
			        	this.header_val = complete_response;
			        	if(verbose){
				        	header_response = this.header_top + complete_response + this.header_buttom;
				        	complete_response = "";
			        	}
			        	else{
			        		header_response = " ";
			        		complete_response = "";
			        	}
			        }
			        else{
			        	complete_response = complete_response + input_line + "\n";
			        	}
			        }
			       loop = false;
				}
			}
		
		reader.close();
		return header_response + this.response_top + complete_response + this.response_buttom;
	}
	
	// Method responsible for writing stirng to specifiec directory 
	// with the content passed to it as a string.
	public boolean writeFile(String filename, String content)throws Exception{
		BufferedWriter out = new BufferedWriter(new FileWriter(filename, true));
		out.write(content);
		out.close();
		return true;
	}
}
