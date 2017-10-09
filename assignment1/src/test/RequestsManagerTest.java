package test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import HttpClient.*;

public class RequestsManagerTest {

	@Test
	public final void testGETWithVerbose() {
			RequestsManager req_manager = new RequestsManager();
			String headers = "Connection:Close,Content-type:application/json";
			
	
			
			assertFalse(req_manager.GET("www.httpbin.org", headers, false).contains("Cache-Control: private"));
			
			assertTrue(req_manager.GET("www.httpbin.org", headers, true).contains("Connection: close"));
	}
	

	@Test
	public final void testPOST() {
		
		RequestsManager req_manager = new RequestsManager();
		String headers = "Connection:Close,Content-type:application/json";
				
		System.out.print(req_manager.GET("http://httpbin.org/redirect-to?url=http://www.google.com", headers, true));
	}
	
	@Test
	public final void testRedirectMethod() {
		RequestsManager req_manager = new RequestsManager();
		String headers = "Connection:Close,Content-type:application/json";
				
		System.out.print(req_manager.redirect("Location: http://www.google.com" ));
	
	}
	
	

}
