package test;

import static org.junit.Assert.*;

import org.junit.Test;
import HttpClient.*;

public class StreamManagerTest {

	@Test
	public final void testReadFile() {
		StreamManager m = new StreamManager();
		try{
			String res = m.readFile("src/resources/foo.text");
			
			assertTrue(res.equals("enkldngnjfn\n"));
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	}
	
	@Test
	public final void testWriteFile(){
		StreamManager m = new StreamManager();
		try{
			boolean res = m.writeFile("C:\\Users\\BABO99\\Desktop\\src\\src\\resources\\f2.text", "content one23\n");
			System.out.print(res);
			assertTrue(res);
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	}

}
