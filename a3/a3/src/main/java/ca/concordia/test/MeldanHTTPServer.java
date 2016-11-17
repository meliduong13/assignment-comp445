package ca.concordia.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MeldanHTTPServer {
	
	private static final Logger logger = LoggerFactory.getLogger(MeldanHTTPServer.class);

    public static void main(String args[] ) throws IOException {

        ServerSocket server = new ServerSocket(8080);
        
        while (true) {
        	
        	System.out.println("Listening for connection on port 8080 ....");
        	// Getting a connection from a client and listening 
            Socket clientSocket = server.accept();
            logger.info("New client from {}", clientSocket.getRemoteSocketAddress());
            
            InputStreamReader isr =  new InputStreamReader(clientSocket.getInputStream());
            BufferedReader reader = new BufferedReader(isr); //Can be done in one line with the previous line;
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            
            int timeout = 0;
            String msg = "";
            
     	   	String st = reader.readLine();
     	   	String [] arr = st.split(" ");
     	   	for (int i=0; i<arr.length;i++)
     	   		System.out.println(arr[i]);


     	   	
            // Responding to client according to what we have received
            Date today = new Date();
            Date expires = addDays(today, 2); // constructor deprecated, change with something else later
            final File dir = new File("C:\\Users\\Mel\\Documents\\fall2016\\comp445\\a3\\a3\\a3");
            if(arr[0].equals("GET"))
            {
            	if(arr[1].equals("/"))
            	{
            		out.write("HTTP/1.0 200 OK\r\n");
                    out.write("Date: "+(today)+"\r\n");
                    out.write("Server: Meldan Server/0.7.6\r\n");
                    out.write("Content-Type: text/html\r\n");
                    out.write("Content-Length: 150\r\n");
                    out.write("Expires: "+(expires)+"\r\n");
                    out.write("Last-modified: "+(today)+"\r\n");
                    out.write("\r\n");
                    out.write("<TITLE>Lab 2</TITLE>");
                    out.write("<P>Listing files in the data directory:</P>");
                    out.write("<P>"+listFilesInDirectory(dir)+"</P>");
            		
            	}
            	else if(arr[1].contains("/"))
            	{
            		String fileName = arr[1].substring(arr[1].indexOf('/')+1);
            		System.out.println("my file name is "+fileName);
            		 final File myFile = new File(dir.getPath() + "\\" + fileName);
            		 System.out.println(myFile);
            	      byte[] mybytearray = new byte[(int) myFile.length()];
            	      try{
            	    	  BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
            	    	  bis.read(mybytearray, 0, mybytearray.length);
            	    	  OutputStream os = clientSocket.getOutputStream();
                	      os.write(mybytearray, 0, mybytearray.length);
                	      os.flush();
                	      os.close();
            	    	  bis.close();
            	      }
            	      catch(FileNotFoundException e)
            	      {
            	    	  out.write("HTTP/1.0 200 OK\r\n");
                          out.write("Date: "+(today)+"\r\n");
                          out.write("Server: Meldan Server/0.7.6\r\n");
                          out.write("Content-Type: text/html\r\n");
                          out.write("Content-Length: 150\r\n");
                          out.write("Expires: "+(expires)+"\r\n");
                          out.write("Last-modified: "+(today)+"\r\n");
                          out.write("\r\n");
                          out.write("<TITLE>HTTP ERROR 404</TITLE>");
                          out.write("<P>HTTP ERROR 404</P>");
                          System.out.println("HTTP ERROR 404");
            	      }
            	      
            	      
            	      
            	     
            	      
            	}
            }
            else if(arr[1].equals("post"))
            {
            	/**  TO MODIFY **/
       		   System.out.println("Message from Client: " + st);
       		  String message = ""; 
       		   //retrieve file name
       		  String fileName= arr[2].substring(arr[2].lastIndexOf('/')+1);
       		Pattern p = Pattern.compile("\"([^\"]*)\"");
       		Matcher m = p.matcher(st);
       		while (m.find()) {
       			//Get content in quotations and assign it to String message
       		  message = m.group(1);
       		}
       		  
       		PrintWriter printWriter = new PrintWriter (fileName);
    		   printWriter.println (message);
       		   printWriter.close (); 
        		
        		 /**  TO MODIFY END **/
            }
            
            out.close();
            reader.close();
            clientSocket.close();
            
        }
    }
    
    public static String listFilesInDirectory(final File dir) {
    	String str = "";
        for (final File fileEntry : dir.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesInDirectory(fileEntry);
            } else {
                str += fileEntry.getName() +" <BR>\n";
            }
        }
        
        return str;
    }
    
    public static Date addDays(Date date, int days)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); //minus number would decrement the days
        return cal.getTime();
    }

}




