package test;

import static org.junit.Assert.*;
import HttpClient.*;
import org.junit.Test;

public class UtilitiesTest {

	@Test
	public final void testParcerParseUrlMethod(){
		String[] str = {"-post=http://www.google.com/post","-f=kj", "-o=jhjh"};
		
		Utilities parser =  new Utilities();
		
		String str1 = "http://www.google.com/post";
		String str2 = "http://google.com";
		String str3 = "www.google.com";
		
		assertEquals("/post", (parser.parseURL(str1))[1]);
		assertEquals("www.google.com", (parser.parseURL(str1))[0]);
		assertEquals("www.google.com", parser.parseURL(str2)[0]);
		assertEquals("www.google.com", parser.parseURL(str3)[0]);
	}
	
	@Test
	public final void testParcerParseSublinkMethod(){
		String[] str = {"-post=url","-f","-d"};
		Utilities parser =  new Utilities();
		
		assertEquals("/firstSub/SecondSub?q=firstInput", 
				parser.parseSubLinks("www.google.com/firstSub/SecondSub?q=firstInput"));
		
		assertEquals("/firstSub/SecondSub?q=firstInput", 
				parser.parseSubLinks("www.google.com/firstSub/SecondSub?q=firstInput"));
		
		assertFalse(parser.parseSubLinks("www.google.com/firstSub/SecondSub?q=firstInput")
				.equals("firstSub/SecondSub?q=firstInput"));
	}
	
	
}
