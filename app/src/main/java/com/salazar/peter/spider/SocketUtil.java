/**
 * 
 */
package com.salazar.peter.spider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List; 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map; 
import java.util.HashMap; 
import java.util.Set; 
import java.util.LinkedHashSet;

import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document; 


/**************************************************
*<b>Title</b>: SocketUtil 
*<b>Project</b>: Intro to Programming Spider
*<b>Description: </b> Class handling socket operations
*<b>Copyright:</b> Copyright (c) Sep 15, 2023
*<b>Company:</b> Silicon Mountain Technologies 
*@author Peter Salazar
*@version 1.0
*@since Sep 15, 2023
*@updates:
*************************************************/
public class SocketUtil {
	
	public static void main (String[] args) {
		
		URL url;
		try {
			url = new URL("https://smt-stage.qa.siliconmtn.com/admintool?cPage=stats&actionId=FLUSH_CACHE");
			SocketUtil su = new SocketUtil(url); 
			String res = su.loginController("peter.salazar@siliconmtn.com", "smtrul3s"); 
			System.out.println(res); 
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		

	}
	
	private Logger logger = Logger.getLogger(SocketUtil.class.getName()); 
	private String host; 
	private String path; 
	private String fileStr;
	private SSLSocket sslSocket; 
	
	
	/**
	 * Constructor - establishes socket connection with passed-in url
	 * @param url
	 */
	public SocketUtil(URL url) {
		// Set member var host
		host = url.getHost(); 
		path = url.getPath(); 
		fileStr = url.getFile(); 
		// init sslsocket factory to create socket
		SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		// try/catch block to initiate socket connection
		try {
			// init ssl socket connection
			sslSocket = (SSLSocket) sslsocketfactory.createSocket(host, 443);
			// logger printout if connection was successful
			logger.log(Level.INFO, "Connection secured to " + url.toString());
		} catch (IOException ioe) {
			// logger printout if exception was encountered
			logger.log(Level.INFO, "IO Exception", ioe);
		}
	}
	
	/**
	 * 
		 * Controller method that implements the login & download procedure
		 * @param String email & String password (login credentials)
		 * @return String html of page behind login
	 */
	public String loginController(String email, String password) {
		String request = String.format("requestType=reqBuild&pmid=ADMIN_LOGIN&emailAddress=%s&password=%s&l=", email, password);
		StringBuilder response = new StringBuilder(); 
		Map <String, String> cookieMap = new HashMap<String,String>(); 
		String[] cookie; 
		Set<String> cookieSeq = new LinkedHashSet<>(); 
		String cookieStr; 
		String location = ""; 
	    StringBuilder finalHTML = new StringBuilder();
        String responseLine;


		try {
	        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sslSocket.getOutputStream()));
	        BufferedReader reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
	        
	        // POST request
	        writer.write(String.format("POST %s HTTP/1.1\r\n", path)); 
	        writer.write(String.format("Host: %s\r\n", host)); 
	        writer.write("Content-Type: application/x-www-form-urlencoded\r\n");
	        writer.write(String.format("Content-Length: %s\r\n\r\n", request.length())); 
	        writer.write(String.format("%s\r\n", request));
	        writer.flush();
	        
	        while ((responseLine = reader.readLine()) != null) {
	            response.append(responseLine + "\n");
	            if (responseLine.matches("^Set-Cookie:\\s(\\S+=\\S+);.*$")) {
	            	cookie = grabCookie(responseLine); 
	            	cookieMap.put(cookie[0], cookie[1]);  
	            	cookieSeq.add(cookie[0]); 
	            }
	        }
	        cookieStr = assembleCookieString(cookieMap, cookieSeq.toArray(new String[cookieSeq.size()])); 
	        
	        // GET request with cookies
			SSLSocketFactory sslsocketfactory2 = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket sslSocket2 = (SSLSocket) sslsocketfactory2.createSocket(host, 443);
	        BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(sslSocket2.getOutputStream()));
	        BufferedReader reader2 = new BufferedReader(new InputStreamReader(sslSocket2.getInputStream()));
	        writer2.write(String.format("GET %s HTTP/1.1\r\n", path)); 
	        writer2.write(String.format("Referrer: https://%s%s\r\n", host, path)); 
	        writer2.write(String.format("Host: %s\r\n", host)); 
	        writer2.write(String.format("Cookie: %s\r\n\r\n", cookieStr));
	        writer2.flush();	    
	        while ((responseLine = reader2.readLine()) != null) {
	        	response.append(responseLine + "\n"); 
	            if (responseLine.matches("^Set-Cookie:\\s(\\S+=\\S+);.*$")) {
	            	cookie = grabCookie(responseLine); 
	            	cookieMap.put(cookie[0], cookie[1]);  
	            	cookieSeq.add(cookie[0]); 
	            }
	            if (responseLine.startsWith("Location:")) location = grabLocation(responseLine); 
	        }
	        cookieSeq.remove("smt.admin.loginComplete");
	        cookieStr = assembleCookieString(cookieMap, cookieSeq.toArray(new String[cookieSeq.size()])); 
	        
		    // Final GET request with additional cookies & redirect
			SSLSocketFactory sslsocketfactory3 = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket sslSocket3 = (SSLSocket) sslsocketfactory3.createSocket(host, 443);
	        BufferedWriter writer3 = new BufferedWriter(new OutputStreamWriter(sslSocket3.getOutputStream()));
	        BufferedReader reader3 = new BufferedReader(new InputStreamReader(sslSocket3.getInputStream()));
	        writer3.write(String.format("GET %s HTTP/1.1\r\n", location)); 
	        writer3.write(String.format("Referrer: https://%s%s\r\n", host, path)); 
	        writer3.write(String.format("Cookie: %s\r\n", cookieStr));
	        writer3.write(String.format("Host: %s\r\n\r\n", host)); 
	        writer3.flush();	    
	        while ((responseLine = reader3.readLine()) != null) {
	        	response.append(responseLine + "\n"); 
	        	if (responseLine.matches("^Set-Cookie:\\s(\\S+=\\S+);.*$")) {
	            	cookie = grabCookie(responseLine); 
	            	cookieMap.put(cookie[0], cookie[1]);  
	            	cookieSeq.add(cookie[0]); 
	            }
	            if (responseLine.startsWith("Location:")) location = grabLocation(responseLine);
	        }
	        cookieStr = assembleCookieString(cookieMap, cookieSeq.toArray(new String[cookieSeq.size()])); 
	        
	        // Final GET request for target page
 			SSLSocketFactory sslsocketfactory4 = (SSLSocketFactory) SSLSocketFactory.getDefault();
 			SSLSocket sslSocket4 = (SSLSocket) sslsocketfactory3.createSocket(host, 443);
 	        BufferedWriter writer4 = new BufferedWriter(new OutputStreamWriter(sslSocket4.getOutputStream()));
 	        BufferedReader reader4 = new BufferedReader(new InputStreamReader(sslSocket4.getInputStream()));
 	        writer4.write(String.format("GET %s HTTP/1.1\r\n", fileStr)); 
 	        writer4.write(String.format("Host: %s\r\n", host)); 
 	        writer4.write(String.format("Cookie: %s\r\n\r\n", cookieStr));
 	        writer4.flush();	    
 	        while ((responseLine = reader4.readLine()) != null) {
 	        	finalHTML.append(responseLine + "\n"); 
 	        }
	  
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return finalHTML.toString(); 
		
	}
	
	/**
	 * 
		 * Helper method for grabbing location header when provided http response
		 * @param String line read in from BufferedReader
		 * @return String representing redirect location
	 */
	private String grabLocation(String line) {
		Matcher matcher = Pattern.compile("^Location:\\s(\\S+)$").matcher(line);
		return matcher.find() ? matcher.group(1) : "";
	}
	
	/**
	 * 
		 * Helper method for assembling a series of cookies into a string to be used for a new request
		 * @param Map<String,String> cookieMap - a map of current cookie keys and values
		 * @param String[] keyOrder - array of strings specifying the order of cookie insertion
		 * @return String - cookie values assembled into a single String
	 */
	private String assembleCookieString(Map<String,String> cookieMap, String[] keyOrder) {
		// init list to incrementally hold reassembled cookies
		var cookieList = new ArrayList<String>(); 
		// iterate over the keys in the provided keyOrder array
		for (String key: keyOrder) {
			// Append each key together with its associated value 
			cookieList.add(key + "=" + cookieMap.get(key));
		}
		// return the String concatenated from the list with appropriate delimiter
		return String.join("; ", cookieList); 
	}
	
	/**
	 * 
		 * Helper method for grabbing cookie provided by http response
		 * @param String line read in from BufferedReader
		 * @return String[2] of cookie in the form [key, value]
	 */
	private String[] grabCookie(String line) {
		// init matcher set to Set-Cookie pattern from HTTP response
		Matcher matcher = Pattern.compile("^Set-Cookie:\\s(\\S+=\\S+);.*$").matcher(line);
        // If a match has been found, return the first match
		String res = matcher.find() ? matcher.group(1) : ""; 
		return res.split("="); 
	}
	
	/**
	 * 
		 * Method to retrieve html from webpage
		 * @return String containing raw html
	 */
	public String getHTML() {
		// init StringBuilder to hold output html
		StringBuilder html = new StringBuilder(); 
		// init String variable to hold lines as we go
		String inLine; 
		try {
			// init BufferedReader object to read data from socket
			BufferedReader incoming = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
			// init BufferedWriter to send data via socket
			BufferedWriter outgoing = new BufferedWriter(new OutputStreamWriter((sslSocket.getOutputStream()))); 
			// send GET request via outputstream
			outgoing.write("GET " + path + " HTTP/1.1\r\nHost: " + host + "\r\n\r\n"); 
			outgoing.flush();
			// Continue reading lines and appending them to the output 
			while(((inLine = incoming.readLine()) != null))
				html.append(inLine + "\n");
			
		} catch (IOException ioe) {
			logger.log(Level.INFO, "IO Exception", ioe);
		}
		return html.toString(); 
	}
}
