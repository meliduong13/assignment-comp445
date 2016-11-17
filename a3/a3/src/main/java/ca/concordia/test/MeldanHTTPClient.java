package ca.concordia.test;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;


public class MeldanHTTPClient {

    public static boolean regexMatch(String input, String regex)
    {
    	Pattern p=Pattern.compile(regex);
    	Matcher m=p.matcher(input);
    	return m.matches();
    }
    
	public static void main(String[] args) throws UnknownHostException, IOException 
	{
		
		String cmd = new String(); //String used to store user input
		@SuppressWarnings("resource")
		Scanner kb = new Scanner(System.in);
		
		
		do
		{
			System.out.println("Enter Meldan commands and press enter. For help enter help, \\h, or ?");
			System.out.println("For Lab 2 commands do the following:");
			System.out.println("1) meldan get localhost/ --> Returns a list of the current files in the data directory");
			System.out.println("2) meldan get localhost/bestWish.txt --> Returns the content of the file bestWish.txt in the data directory");
			System.out.println("3) meldan post localhost/bestWish.txt \"content\" --> Rewrites the file bestWish.txt with the content written by the user");
			cmd = kb.nextLine();
			//System.out.println(cmd);
			String [] arr=cmd.split(" ");
			
			
			// Main commands 
			switch(arr[0]) 
			{
			case "meldan": meldan(arr);  break;
			case "help" :  case "\\h": case "?": help(); break;
			case "exit" : break;
			default : System.out.println("Please enter a valid command.");
			}
		
		}while(!cmd.equals("exit"));
		
	} //END OF MAIN

   
  public static void meldan(String[] myArr) throws IOException
  {
	  	String [] arr = myArr;
	  	String evaluate = "";
	  	for(int i=0; i < arr.length; i++)
	  	{
	  		evaluate += arr[i] + " ";
	  	}
	  	
	  	String address = "";
	  	
	  	if(evaluate.contains("localhost") || evaluate.contains("127.0.0.1"))
	  		address = arr[2];
	  	else
	  		address = arr[arr.length-1];
	  	
	  	
	  	String domainName = getDomain(address);  // "localhost"; 
	  	
	  	int port = 80;
	  	
	  	if(domainName.equals("localhost") || domainName.equals("127.0.0.1"))
		{
			port = 8080;   
		}
		
		
		
		Socket s = new Socket();
		try
		{
			s.connect(new InetSocketAddress(domainName, port));
			
		}
		catch(UnknownHostException e)
		{
			System.err.println("Unknown host. Please check that the url you are inputting is correct.");
			e.printStackTrace();
			System.exit(0);
		}
		
		if(arr[1].equals("get"))
			get(s, arr, address);
		
		if(arr[1].equals("post"))
		post(s, arr, address);
		
		
  }//END OF METHOD MELDAN
  
  
  private static void get(Socket s, String[] arr, String address) throws IOException
  {
	  String extension = getExtension(address);
	
		PrintWriter pw = new PrintWriter(s.getOutputStream());
		InputStream myInStream = s.getInputStream();
	    //BufferedReader br = new BufferedReader(new InputStreamReader(myInStream, "UTF8"));
			
		
		int indexofh=0;
		String keyvalue=null;
		pw.print("GET "+extension+" HTTP/1.0\r\n");
		
		for (int i=0;i<arr.length;i++)//assign key value if applicable, otherwise they are null
	    	{
	    		if (arr[i].equals("-h"))
	    		{
	    			indexofh=i;
	    			if (arr[indexofh+1]!=null && arr[indexofh+1].contains(":")){//key value entry is at the index following that of -h 
	    				{
	    				keyvalue=arr[indexofh+1];
	    				 if (indexofh!=0)
	    				    {
	    					pw.print (keyvalue+"\r\n"); 
	    				    }
	    				}
	    			}
	    			else;//if there is a command -h is not properly typed with key value, key value are null by default
	    			
	    		}
	    		else;//if there is no command -h skip this part, key and value are null by default
	    			
	    	
	    	}//end of for
		 pw.println("\r\n");
		pw.flush();
		
				
	//read from server and print ******************************************************
		
		readfromserver(arr, myInStream, s);
		s.close();
		
  } //END OF METHOD GET
  
  
  
  private static void post(Socket s, String[] arr, String address) throws IOException
  {
	  String extension = getExtension(address);
	  //System.out.println("ext: "+extension);
	  String data =null;
	  BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), "UTF8"));
	  
	  int indexofh=0;
	  String keyvalue=null;
	  
	  boolean thereIsAnF = false;
	  
	  for (int i=0;i<arr.length;i++)
	  {
		  if (arr[i].equals("-f"))
		  {
			  thereIsAnF = true; 
			  break;
		  }
	  }
	  
	  if(thereIsAnF)
	  {
		//This code runs when there is a -f option
		  
		  String url = "http://" + address; //"http://example.com/upload";
		  String charset = "UTF-8";
		  String param = "value";
		  File textFile = new File("C:\\Users\\Daniel\\Documents\\cartas\\bestWish.txt");
		  //File binaryFile = new File("C:\\Users\\Daniel\\Documents\\cartas\\bestWish.txt");
		  String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
		  String CRLF = "\r\n"; // Line separator required by multipart/form-data.
		  
		  URLConnection connection = new URL(url).openConnection();
		  connection.setDoOutput(true);
	
		  //Sends custom headers from the user (-h option) before setting 
		  /// the connection request property to multipart/form-data
		  
		  for (int i1=0;i1<arr.length;i1++)
		  {
	    		if (arr[i1].equals("-h"))
	    		{
	    			indexofh=i1;
	    			if (arr[indexofh+1]!=null && arr[indexofh+1].contains(":")) //key value entry is at the index following that of -h 
	    			{
						keyvalue = arr[indexofh+1];
						String[] parts = keyvalue.split(":");
						String key = parts[0]; 
						String value = parts[1]; 
						if (indexofh!=0)
						{
							connection.addRequestProperty(key, value);	
					    }
					    i1++;
	    				
	    			}
	    			
	    		}
		  }//end of for
		  
		  // Sets connection's request property to multipart/form-data to allow the user to send files
		  connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		  
		  OutputStream output = connection.getOutputStream();
		  PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
		  
		  for (int i1=0;i1<arr.length;i1++)
		    {
				if (arr[i1].equals("-f"))
				{
					System.out.println("user file name is: ");
					System.out.println(arr[i1+1]);
					textFile = new File(arr[i1+1]);
					break;
				}
				
		    }//end of for
		  
		   // Send normal parameter.
		    writer.append("--" + boundary).append(CRLF);
		    writer.append("Content-Disposition: form-data; name=\"param\"").append(CRLF);
		    writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
		    writer.append(CRLF).append(param).append(CRLF).flush();

		    // Send text file.
		    writer.append("--" + boundary).append(CRLF);
		    writer.append("Content-Disposition: form-data; name=\"textFile\"; filename=\"" + textFile.getName() + "\"").append(CRLF);
		    writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF); // Text file itself must be saved in this charset!
		    writer.append(CRLF).flush();
		    Files.copy(textFile.toPath(), output);
		    output.flush(); // Important before continuing with writer!
		    writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

		    // Send binary file.
		    /*writer.append("--" + boundary).append(CRLF);
		    writer.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"" + binaryFile.getName() + "\"").append(CRLF);
		    writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
		    writer.append("Content-Transfer-Encoding: binary").append(CRLF);
		    writer.append(CRLF).flush();
		    Files.copy(binaryFile.toPath(), output);
		    output.flush(); // Important before continuing with writer!
		    writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
		    */

		    // End of multipart/form-data.
		    writer.append("--" + boundary + "--").append(CRLF).flush();
		    
		    

		    // Request is lazily fired whenever you need to obtain information about response.
		    //int responseCode = ((HttpURLConnection) connection).getResponseCode();
		    //System.out.println(responseCode); // Should be 200
		  
		        
		//read from server*********************************************************************************************   
		  //BufferedReader rd1 = new BufferedReader(new InputStreamReader(connection.getInputStream())); 
		  readfromserver(arr, connection.getInputStream(), s);
		   
		   
	  }//END OF "thereIsAnF" IF
	  else
	  {
		// This code runs when there is no -f option
		  
		  //wr.write("POST " + extension + " HTTP/1.0\r\n");
		  //System.out.println("arr 2 is " + arr[2] );
		  //System.out.println("extension is " + extension );
		  
		  if(arr[2].contains("localhost") || arr[2].contains("127.0.0.1"))
		  {
			  if(extension.equals("/"))
			  {
				  System.out.println("Incorrect File name. You forgot to add the name of the file you want to post.");
	
			  }
			  else if(extension.contains("/"))
			  {
				  /** TO MODIFY **/
				  extension=extension.substring(1);
				  PrintStream ps = new PrintStream(s.getOutputStream());
		    	  BufferedReader brs = new BufferedReader(new InputStreamReader(s.getInputStream()));
		    	  

		    		  String myCommand="";
		    		 for (int i=0; i<arr.length;i++)
		    		 { if (i!=arr.length-1)
		    				myCommand+=arr[i]+ " "  ;
		    		 	else
		    			  myCommand+=arr[i];
		    		 }
		    		 System.out.println("Sending information to server...");
		    		 ps.println(myCommand);
		    		 // String st = br.readLine();
		    		 // ps.println(st);
		    		  
			      /** TO MODIFY END**/
			  }
			  System.out.println("File successfully created and written to");
		  }
		  else
		  {
			  for (int i1=0;i1<arr.length;i1++)//assign key value if applicable, otherwise they are null
			  {
		    		if (arr[i1].equals("-h"))
		    		{
		    			indexofh=i1;
		    			if (arr[indexofh+1]!=null && arr[indexofh+1].contains(":")) //key value entry is at the index following that of -h 
		    			{
							keyvalue=arr[indexofh+1];
							if (indexofh!=0)
							{
								wr.write (keyvalue+"\r\n"); 
						    }
						    i1++;
		    				
		    			}
		    			
		    		}
			  }//end of for
			  
			  for (int i1=0;i1<arr.length;i1++)//assign key value if applicable, otherwise they are null
			     {
			    		if (arr[i1].equals("-d"))
			    		{
			    			data = arr[i1+1];
			    			if (data!=null)
			    			{
			    				wr.write("Content-Length: " + data.length() + "\r\n");
			    			}
			    			wr.write("\r\n");
			    			wr.write (data); 
			    		}
			    	 
			      }//end of for 2
		  }
		  
//		  if(data!=null)
//				wr.write(data);
//		  
//		  wr.write("\r\n");
		  //wr.write(files);
		  //wr.flush();
		  
		  
		  
		     
		//read from server*********************************************************************************************   
		  //BufferedReader rd1 = new BufferedReader(new InputStreamReader(s.getInputStream())); 
		  readfromserver(arr, s.getInputStream(), s);
		  //wr.flush();
	  }//End of "threIsAnF" ELSE   Meaning that inside these brackets there is no F
		  
	    
		
	  //wr.write("\r\n");
	 // wr.flush();
	  // wr.close(); 
	  s.close();
	  
  } //ENF OF METHOD POST
  
  
  //readFully reads until the request is fulfilled or the socket is closed
  private static void readFully(SocketChannel socket, ByteBuffer buf, int size) throws IOException {
      while (buf.position() < size) {
          int n = socket.read(buf);
          if (n == -1) {
              break;
          }
      }
      if (buf.position() != size) {
          throw new EOFException();
      }
  }
  
  private static void readfromserver(String [] arr, InputStream inStream, Socket socket) throws IOException
  {
	  String address = arr[arr.length-1];
	  String domainName = getDomain(address);
	  
	  
	  String t = null;
	  
	  Charset utf8 = StandardCharsets.UTF_8;
	 
	  BufferedReader br = new BufferedReader(new InputStreamReader(inStream, "UTF-8")); 
	  
	  if(domainName.equals("localhost") || domainName.equals("127.0.0.1"))
	  {
		 
          System.out.println("Response from server: "  );
          while ((t = br.readLine()) != null) 
		    {
		      System.out.println(t);
		    }
		    
		    
		  
	  }
	  else
	  {
		  
	  
			if (arr[2].equals("-v"))
			{
				    while ((t = br.readLine()) != null) 
				    {
				      System.out.println(t);
				    }   
			}
			else
			{
			    	while((t = br.readLine()) != null)
					{
						if (t.contains("{"))
						{
							System.out.println(t);
							while ((t=br.readLine())!=null)
									System.out.println(t);		
						}
								
					}
			    	
			 }
		
		
	  }
	
	  br.close();
	   
	  
  }
  
  public static byte[] read(InputStream istream)
  {
	    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[16384]; // Experiment with this value
    int bytesRead;

    try {
		while ((bytesRead = istream.read(buffer)) != -1)
		{
			 
		  baos.write(buffer, 0, bytesRead);
		}
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

    	try {
			baos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    return baos.toByteArray();
  }
  
  private static String getDomain(String address)
  {
	  	if(address.startsWith("localhost") || address.startsWith("127.0.0.1"))
	  	{
	  		return "localhost";
	  	}
	  	
	  	String preExtension = address.substring(address.lastIndexOf('.')+1);

	  	String cutPre = "";
	  	try{
	  		cutPre = preExtension.substring(0, preExtension.indexOf('/'));
	  	}
	  	catch (StringIndexOutOfBoundsException c)
	  	{
	  		preExtension += "/";
	  		cutPre = preExtension.substring(0, preExtension.indexOf('/'));
	  	}
	  	
	  	String domainName = address.substring(0, address.lastIndexOf('.')+1) + cutPre;
	  	return domainName; 
  }
  
  private static String getExtension(String address)
  {
	  String extension;
	  
	 if(address.startsWith("localhost") || address.startsWith("127.0.0.1"))
	  {
		 extension = address.substring(address.indexOf('/'));
	  }
	 else
	 {
		 String preExtension = address.substring(address.lastIndexOf('.')+1);
		 extension = preExtension.substring(preExtension.indexOf('/'));		
	 }
	 
	  return extension;
	  
  }
  
  public static void help()
  {
	  System.out.println("Enter a command in the following format: \n" +
	  		  "meldan get -v -h a:b -h c:d url/extension \nNote: The url does not contain \"http://\" \n" + 
			  "Example 1: meldan get -v -h a:b httpbin.org/get?course=networking&assignment=1 \n" +
	  		  "Example 2: meldan get -v -h Content-Type:application/json -h a:b -h c:d -h z:2 google.ca/?gws_rd=cr&ei=qQr4V6PzGYiF-wHFlqeICg#q=hello \n" +
			  "Example 3: meldan post -h Content-Type:application/json -d '{\"Assignment\":_1}' httpbin.org/post \n" +
	  		  "Example 4: meldan post -h a:b -f C:\\Users\\Daniel\\Documents\\fileName.txt httpbin.org/post");

	  System.out.println("Enter the command 'exit' to end session.");
  }
  
  
    	
}
