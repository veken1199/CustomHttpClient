package HttpClient;

import java.util.HashMap;
import java.util.Map;

public class Utilities {
	// Method responsible for convertion json like string to map 
		// in a key-value form
		public Map<String, String> mapConverter(String str){
			Map<String, String> map = new HashMap<String, String>();
			
			// Step 0 - return empty map if the parameter is empty
			if(str.length()==0){
				return map;
			}
			
			// Step 1 - breaking {x:y , k:l} into array of strings where comma appears
			String[] items = str.split(",");
			
			// Step 2 - Break each item into key and value and store it in the map
			for(String item : items){
				String[] arr = item.split(":");
				map.put(arr[0], arr[1]);
			}
			
			// Step 3 - Return the map 
			return map;
		}
		
		// Method responsible for parsing the url and extracting the host/sublinks/queries
		public String[] parseURL(String str){
			
			//Step 1 remove 'http://' if does exists
			if(str.contains("http://")){
				str = str.substring(str.indexOf("http://")+7);
			}
			
			//Step 2 add www. if does not exist
			if(!str.contains("www.")){
				str = "www." + str;
			}
			
			//Step 3 find the directory if any
			String parsed_dir = this.parseSubLinks(str);
			
			//Step 4 remove the sublink from the url
			if(str.contains("/")){
				str = str.substring(0,str.indexOf('/'));
			}
			return new String[]{str, parsed_dir};
		}
		
		
		// Method reponsible for returning the subdirectories from a url
		// Assumed that this method is always invoked after parseUrl method
		public String parseSubLinks(String url){
			String sublink = "/";
			
			if(url.contains("/")){
				// Find where the first '/' and return the sublink starting from that given position
	 			sublink = url.substring(url.indexOf('/'));
			}
			
			return sublink;
		}
		
		
}
