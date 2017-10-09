package HttpClient;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import static  java.util.Arrays.asList;


public class Parser {
	
	private RequestsManager requests_manager;
	private OptionParser parser;
	public OptionSet opts;
	private CommandsValidator validator;
	private Boolean verbose;
	private StreamManager stream_manager;
	private Utilities utility;
	
	// Constructor
	public Parser(String[] args, RequestsManager request_manager){
		
		this.parser = new OptionParser();
		this.stream_manager = new StreamManager();
		this.requests_manager = request_manager;
		
		// Populating the commands list
		this.createCommands();
		
		// Passing java arguements into the parser
		this.opts = parser.parse(args);
		
		// Validating the commands
		this.validator = new CommandsValidator(opts);
		
		// Check if the verbose is requested
		this.verbose = opts.has("v");	
	}
	
	
	// Method responsible for creating the commands 
	private void createCommands(){

		parser.acceptsAll(asList("get", "GET", "Get"), "TimeServer hostname")
        .withOptionalArg()
		.defaultsTo("");
        
		parser.acceptsAll(asList("post", "POST", "Post"), "TimeServer hostname")
        .withOptionalArg()
		.defaultsTo("")
		.requiresArgument();
	
		parser.acceptsAll(asList("verbose", "v"), "Verbosety")
        .withOptionalArg()
		.requiresArgument();
		
		parser.acceptsAll(asList("header", "h", "Header"), "HTTP request headers in for of key:value")
		.withOptionalArg()
		.defaultsTo("")
		.requiresArgument();
		
		
		parser.acceptsAll(asList("f"), "Associates the content of a file to the body HTTP POST request.")
		.withOptionalArg()
		.requiresArgument();
		
		parser.acceptsAll(asList("d"), "Associates the content of a file to the body HTTP POST request.")
		.withOptionalArg()
		.requiresArgument();
		
		parser.acceptsAll(asList("o"), "Associates the content of a file to the body HTTP POST request.")
		.withOptionalArg()
		.defaultsTo("")
		.requiresArgument();
		
		parser.acceptsAll(asList("help"), "Gereral usage")
        .withOptionalArg()
        .defaultsTo("");
	}
	
	
	// This Method responsible for handling and processing the commands
	public void handleCommands(){
		
		// Handling Help commands
		CommandsValidator validator = new CommandsValidator(opts);
		if (opts.has("help")){
			
			if( opts.valueOf("help").toString().compareToIgnoreCase("get") == 0 ) {
				System.out.print(opts.valueOf("help"));
				getHelp();
			}
		
			else if( opts.valueOf("help").toString().compareToIgnoreCase("post") == 0 ) {
				System.out.print(opts.valueOf("help"));
				postHelp();
			}
			
			else{
				help();
			}
		}
		
		// Handling POST commands
		else if(opts.has("post")){
			if(!validator.validatePostCommand()){
				validator.printErrorList();
			}
			else{
				try{
				// read the inline/file data 
					String data = this.readPostData(opts);
					String response = requests_manager.POST(opts.valueOf("post").toString(), opts.valueOf("h").toString(), verbose, data);
					
					if(opts.has("o")){
						stream_manager.writeFile(opts.valueOf("o").toString(), response);
						System.out.println("NOTE: The response is stored in a file.");
					}
					
					System.out.println(response);
				}
				catch(Exception e){
					System.out.println(e.getMessage());
				}
			}
		}
		
		// Handling GET commands
		else if(opts.has("get")){
			if(!validator.validateGetCommand()){
				validator.printErrorList();
			}
			
			
			else{
				try{
					String response = requests_manager.GET(opts.valueOf("get").toString(), opts.valueOf("h").toString(), verbose);
				
					if(opts.has("o")){
						stream_manager.writeFile(opts.valueOf("o").toString(), response);
						System.out.println("NOTE: The response is stored in a file.");
					}
					
					System.out.println(response);
				}
				
				catch(Exception e){
					System.out.println(e.getMessage());
				}
			}	
		}
	}
	
	
	private String readPostData(OptionSet opts) {
		String data = "";
		int flag = 1; // 1 stadands for reading from file 
		
		if (opts.has("d")){
			flag = 0;
		}
		
		switch (flag){
		
		// Read from a file
		case 1 : {
			try{
				StreamManager sm = new StreamManager();
				data = sm.readFile(opts.valueOf("f").toString());
				break;
			}
			catch(Exception e){
				System.out.println(e.getMessage());
			}
			finally{
				
			}
		}
		
		// Read from console 
		case 0 : {
				data = opts.valueOf("d").toString();
				break;
			}
		}
		return data;
	}


	// Displaying GET help info
	public void getHelp(){
		System.out.println("httpc -help get \n");
		System.out.println("usage:");
		System.out.println("httpc -get [-v] [-h key:value] URL \n");
		System.out.println("-get \t executes a HTTP GET request for a given URL.");
		System.out.println("-v \t Prints the detail of the response such as protocol, status, and headers.");
		System.out.println("-h \t key:value Associates headers to HTTP Request with the format 'key:value'.");
		System.out.println("\n");
	}
	
	
	// Displaying POST help info
	public void postHelp(){
		System.out.println("httpc -help post \n");
		System.out.println("usage:");
		System.out.println("httpc -post [-v] [-h key:value] [-d inline-data] [-f file] [-o filename] URL");
		System.out.println("-post \t executes a HTTP POST request for a given URL with inline data or from file.");
		System.out.println("-v \t Prints the detail of the response such as protocol, status, and headers.");
		System.out.println("-h \t key:value Associates headers to HTTP Request with the format 'key:value'.");
		System.out.println("-d \t string Associates an inline data to the body HTTP POST request.");
		System.out.println("-f \t file Associates the content of a file to the body HTTP POST request.");
		System.out.println("-o \t file to be contain the data reponse of the post request");
		System.out.println("Note: Either [-d] or [-f] can be used but not both.");
	}
	
	
	// Displaying help info
	public void help(){
		System.out.println("httpc is a curl-like application but supports HTTP protocol only. \n");
		System.out.println("Usage: httpc 'command' [arguments]. The commands are:");
		System.out.println("-get \t executes a HTTP GET request and prints the response.");
		System.out.println("-post \t executes a HTTP POST request and prints the response.");
		System.out.println("-help \t prints this screen.");
		System.out.println("Use 'httpc -help [command]' for more information about a command.");
	}	
}

