# Http Client



 implemented a simple HTTP client applicationin Java and experiment it in real HTTP Servers (web servers).

  - Get Request
  - Post Request
  - Help commandline
  - Redirect 
  - Read and Write to files
  - Custom Headers
  - Custom Parser with JobTSimple library

# Using the application:

  - java -jar Httpc.jar -help
  - java -jar Httpc.jar -help post
  - java -jar Httpc.jar -help get
  - java -jar Httpc.jar -get "www.google.com" -h"Connection:Close" -v
  - java -jar Httpc.jar -get "www.google.com" -h"Connection:Close" -v -o"C:\Users\xxxxx\Desktop\src\src\resources\getGoogle1.text"
  - java -jar Httpc.jar -post "www.google.com" -h"Connection:Close" -v -o"C:\Users\xxxxx\Desktop\src\src\resources\getGoogle1.text"
  - java -jar Httpc.jar -post "http://httpbin.org/post" -h"Connection:Close" -v -o"C:\Users\xxxx\Desktop\src\src\resources\postGoogle1.text" -d"Connection:foo"
  - java -jar Httpc.jar -post "http://httpbin.org/post" -h"Connection:Close" -v -o"C:\Users\xxxx\Desktop\src\src\resources\postGoogle1.text" -f"C:\Users\xxxx\Desktop\src\src\resources\postGoogle1.text"



