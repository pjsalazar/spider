/**
 * 
 */
package com.salazar.peter.spider;

import java.io.File;
import java.io.BufferedWriter; 
import java.io.FileWriter;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element; 

/**************************************************
*<b>Title</b>: FileUtil 
*<b>Project</b>: Intro to Programming Spider
*<b>Description: </b> Class handling file operations
*<b>Copyright:</b> Copyright (c) Sep 18, 2023
*<b>Company:</b> Silicon Mountain Technologies 
*@author Peter Salazar
*@version 1.0
*@since Sep 18, 2023
*@updates:
*************************************************/
public class FileUtil {
	
	
	private File outFile; 
	
	/**
	 * Constructor - sets member variable File based on passed in location & name
	 * @param File dir - the directory location
	 * @param String fileName - the file name
	 */
	public FileUtil(File dir, String fileName) {
		// if fileName is empty or non-character, set it as root plus character
		if (fileName == null || fileName.isEmpty() || fileName.equals("/")) fileName = "root"; 
		else if (fileName.matches("^\\\\W.*$")) fileName = "root" + fileName; 
		// set destination file hooked up to full location
		outFile = new File(dir, fileName + ".html");
	}
	
	
	/**
	 * 
		 * Writes HTML of a provided Jsoup document to member var location
		 * @param Document contents - Jsoup document to be written to file
	 */
	public void writeHTML(Document contents) {
		// Init BufferedWriter set to outFile
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))){
			// write the Stringified html of contents
			writer.write(contents.toString());
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
