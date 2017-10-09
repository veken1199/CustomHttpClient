package test;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import HttpClient.*;

public class ParserTest {
	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
	
	
	@Before
	public void setUpStreams() {
	    System.setOut(new PrintStream(outContent));
	    System.setErr(new PrintStream(errContent));
	}

	@After
	public void cleanUpStreams() {
	    System.setOut(null);
	    System.setErr(null);
	}
	
	@Test
	public final void testMapConverter() {
		String[] str = {"asda"};
		Utilities utility = new Utilities();
		
		Map<String, String> map = utility.mapConverter("key 1:val 1,key 2:val 2");
		
		assertTrue(map.get("key 1").equals("val 1"));
		assertTrue(map.get("key 2").equals("val 2"));
		assertFalse(map.get("key 2").equals(" val 2"));
	}
	
	@Test
	public final void testMapConverterInvalidSpacedStrings(){
		String[] str = {"asda"};
		Utilities utility = new Utilities();
		
		Map<String, String> map = utility.mapConverter("key 1:val 1,key 2:val 2");
		
		assertTrue(map.get("key 1").equals("val 1"));
		assertTrue(map.get("key 2").equals("val 2"));
		assertFalse(map.get("key 2").equals(" val 2"));
	}
	
	@Test
	public final void testMapConverterInvalidEmptString(){
		String[] str = {"asda"};
		Utilities utility = new Utilities();
		
		Map<String, String> map = utility.mapConverter("");
		
		assertTrue(map.isEmpty() == true);
	}
	
	@Test
	public final void testParcerHandlingCommandsMethod(){
		String[] str = {"-help"};
		Parser parser =  new Parser(str, new RequestsManager());
		
		parser.handleCommands();
		
		String output = outContent.toString();
		
		assertTrue(output.contains("httpc is a curl-like application but supports HTTP protocol only"));
		}
	
	@Test
	public final void testParcerHandlingCommandsMethodGetHelp(){
		String[] str = {"-help=get"};
		Parser parser =  new Parser(str, new RequestsManager());
		
		parser.handleCommands();
		
		String output = outContent.toString();
		System.out.print(output);
		
		assertTrue(output.contains("httpc -help get"));
	}
	
	@Test
	public final void testParcerHandlingCommandsMethodPostwithoutForD(){
		String[] str = {"-post=url"};
		Parser parser =  new Parser(str, new RequestsManager());
		
		parser.handleCommands();
		
		String output = outContent.toString();
		System.out.print(output);
		
		assertTrue(output.contains("You need to use either '-f' for file, or '-d' for inline input"));
	}
	
	@Test
	public final void testParcerHandlingCommandsMethodPostwithBOTHForD(){
		String[] str = {"-post=url","-f","-d"};
		Parser parser =  new Parser(str, new RequestsManager());
		
		parser.handleCommands();
		
		String output = outContent.toString();
		System.out.print(output);
		
		assertTrue(output.contains("You can not '-f' for file and '-d' for inline input at the same time"));
	}
	
	@Test
	public final void testPostWithReadFile(){
		String [] str = {"-post=http://httpbin.org/post", "-h=Connection:Close", "-v",  "-f=C:\\Users\\BABO99\\Desktop\\src\\src\\resources\\f.text"};
		Parser parser =  new Parser(str, new RequestsManager());
		
		parser.handleCommands();
		
		String output = outContent.toString();
		System.out.print(output);
	
	}
	
	
}
	
