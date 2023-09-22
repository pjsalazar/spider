/**
 * 
 */
package com.salazar.peter.spider;

import java.util.List;
import java.util.ArrayList; 
import java.util.Arrays; 
import java.util.Set;
import java.util.logging.Logger;
import java.util.HashSet; 

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File; 

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL; 

import javax.swing.text.html.HTMLDocument;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document; 

/**************************************************
*<b>Title</b>: SpiderService 
*<b>Project</b>: Intro to Programming Spider
*<b>Description: </b> Top-level class that implements the webpage spider
*<b>Copyright:</b> Copyright (c) Sep 11, 2023
*<b>Company:</b> Silicon Mountain Technologies 
*@author Peter Salazar
*@version 1.0
*@since Sep 11, 2023
*@updates:
*************************************************/

public class SpiderService {
	
	public static void main(String[] args) throws IOException, URISyntaxException {
		String url = "https://smt-stage.qa.siliconmtn.com/"; 
		String fileLoc = "/Downloads/"; 
		SpiderService ss = new SpiderService(url, fileLoc);	 
		ss.spider(); 
		
		String targetPage = "https://smt-stage.qa.siliconmtn.com/admintool?cPage=stats&actionId=FLUSH_CACHE"; 
		String email = "";
		String password = "";
		ss.downloadHTMLBehindLogin(targetPage, email, password);
	}
	
	
	private List <URL> urlList; 
	private List <URL> checked = new ArrayList<>(); 
	private File dir; 
	
	
	/**
	 * Constructor
	 * @param String url: starting url
	 * @param String filePath: local directory location wherein to save the contents of each page's html
	 * @throws IOException 
	 */
	public SpiderService(String entryLink, String fileLoc) {
		try {
			// create a url from the String entryLink
			URL url = new URL(entryLink); 
			// Set the entryURL as the first element of urlList
			urlList = new ArrayList <URL>(Arrays.asList(url));
			// init directory at fileLoc wherein to save each html file
			dir = new File(System.getProperty("user.home") + fileLoc + url.getHost());
			// if this directory doesn't exist, create it
			if (!dir.exists()) dir.mkdirs(); 
			
		} catch (MalformedURLException mue) {
			mue.printStackTrace();
		} 
	}
	
	
	/**
	 * 
		 * Controller method that implements the spider procedure
		 * Accesses the input url, saves the html to the provided file location
		 * If any links are contained within the first page's html, repeat this procedure for that url
		 * @param String url: starting url
		 * @param String filePath: local directory location wherein to save the contents of each page's html
	 * @throws IOException 
	 */
	public void spider() throws IOException {	
		// while urlList isn't empty, process the first element
		while(!urlList.isEmpty()) {
			processLink(urlList.get(0));
		}
	}
	
	/**
	 * 
		 * Controller method that downloads a specified page's html after completing the login process, writes to file
		 * @param String targetPage - url to be downloaded
		 * @param String email - login credential
		 * @param String password - login credential
	 */
	public void downloadHTMLBehindLogin(String targetPage, String email, String password) {
		try {
			// init url from targetPage
			URL url = new URL(targetPage);
			// init Socket util hooked up to targetPage
			SocketUtil su = new SocketUtil(url);
			// retrieve html from login procedure and cast into Jsoup Document
			Document html = Jsoup.parse(su.loginController(email, password), targetPage);
			// call helper method writeFile to save file
			writeFile(html, url.getFile());
			
		} catch (MalformedURLException mue) {
			mue.printStackTrace();
		} 

	}
	
	
	public void processLink(URL url) throws MalformedURLException {
		// Call helper method retrieveData (socket procedure); returns contents of page as HTMLDocument
		Document html = retrieveData(url); 
		// Call helper method parseURL (parsing procedure); returns array of links
		List <String> linkList = parseURLLinks(html); 
		// iterate over elements of linkList
		URL newURL; 
		for (String link: linkList) {
			newURL = new URL(link); 
			// add array of links to master urlList if not present in urlList or checked
			if (urlList.stream().noneMatch(i -> i.toString().equals(link)) 
					&& checked.stream().noneMatch(i -> i.toString().equals(link))) urlList.add(newURL); 
		}		
		
		// Call helper method writeFile (saving procedure)
		writeFile(html, url.getPath()); 
		// finished with process, add url to checked and remove form urlList
		checked.add(url); 
		urlList.remove(url); 
	}
	
	
	/**
	 * 
		 * Helper method - sets up socketUtil object, uses that object to retrieve webpage data 
		 * @param URL url
		 * @return JSoup document object
	 */
	private Document retrieveData(URL url) {
		// init socketUtil hooked up to this url
		SocketUtil su = new SocketUtil(url); 
		// retrieve html as a string, parse to Jsoup document and return 
		return Jsoup.parse(su.getHTML(), url.toString()); 
	}
	
	/**
	 * 
		 * Helper method - parses url and returns a list of all links contained within
		 * @param URL url
		 * @return List containing strings corresponding to each link
	 */
	private List<String> parseURLLinks(Document html){
		// init parserUtil with this url
		ParserUtil pu = new ParserUtil(html); 
		List<String> links = pu.getLinks(); 
		// call instance method to extract links, return set
		return links;
	}
	
	/**
	 * 
		 * Helper method - writes html contents out to a file at directory fileLoc
		 * @param Document contents, String fileName
	 */
	private void writeFile(Document contents, String fileName) {
		// init FileUtil with fileLoc and fileName
		FileUtil fu = new FileUtil(dir, fileName); 
		// call instance method to write file contents to indicated location
		fu.writeHTML(contents); 
	}
}
