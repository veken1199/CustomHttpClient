package HttpClient;


// Main driver class
public class httpc {

	public static void main(String[] args)throws Exception{
		
		Parser parser = new Parser(args, new RequestsManager());
		parser.handleCommands();
		
	}
}
