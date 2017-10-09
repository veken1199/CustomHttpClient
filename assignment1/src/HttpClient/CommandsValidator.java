package HttpClient;

import joptsimple.*;
import java.util.ArrayList;

public class CommandsValidator {
	private OptionSet opts;
	public ArrayList <String> error_messages;
	
	// Constructor
	public CommandsValidator(OptionSet opts){
		this.opts = opts;
		error_messages = new ArrayList<String>();
	}
	
	// Method to validate GET command
	public boolean validateGetCommand(){
		boolean flag = true;
		
		if(opts.valueOf("get").equals("")){
			flag = false;
			error_messages.add("Please insert the URL right after the 'get' command");
		}
		
		if(opts.has("o") && opts.valueOf("o").equals("")){
			flag = false;
			error_messages.add("Make sure you give the filename that you want to store the reponse in! avoid '/' or '\'");
		}

		
		return flag;
	}
	
	
	// Method to validate POST command
	public boolean validatePostCommand(){
		boolean flag = true;
		
		if(opts.valueOf("post").equals("")){
			flag = false;
			error_messages.add("Please insert the URL right after the 'post' command");
		}
		
		if(opts.has("d") && opts.has("f")){
			flag = false;
			error_messages.add("You can not '-f' for file and '-d' for inline input at the same time :(");
		}
		
		if(! opts.has("d") && ! opts.has("f")){
			flag = false;
			error_messages.add("You need to use either '-f' for file, or '-d' for inline input");
		}
		
		if(opts.has("o") && opts.valueOf("o").equals("")){
			flag = false;
			error_messages.add("Make sure you give the filename that you want to store the reponse in! avoid '/' or '\'");
		}
		return flag;
	}
	
	// Method to print all the errors recorded after validating commands
	// and clear the error list.
	public void printErrorList(){
		for(String err : error_messages){
			System.out.println(err);
		}
		
		// Clearing the error list
		error_messages.clear();
	}
}
