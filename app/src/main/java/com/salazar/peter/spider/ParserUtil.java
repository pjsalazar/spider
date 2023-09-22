/**
 * 
 */
package com.salazar.peter.spider;

import java.io.IOException;

import java.util.Set; 
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList; 

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**************************************************
*<b>Title</b>: ParserUtil 
*<b>Project</b>: Intro to Programming Spider
*<b>Description: </b> Class handling parsing operations
*<b>Copyright:</b> Copyright (c) Sep 15, 2023
*<b>Company:</b> Silicon Mountain Technologies 
*@author Peter Salazar
*@version 1.0
*@since Sep 15, 2023
*@updates:
*************************************************/
public class ParserUtil {
    
    
    private Document html; 
    
    /**
     * Constructor - sets member variable to input html passed in
     * @param Document html
     */
    public ParserUtil(Document html) {
    	this.html = html; 
    }
    
    
    /**
     * 
    	 * Extracts links from member var html
    	 * @return List<String> containing links parsed
     */
    public List<String> getLinks(){
    	// Access the links contained within instance variable html
    	Elements links = html.select("a[href]");
    	// init output set
    	List<String> output = new ArrayList<String>(); 
    	// iterate over collection of lists
    	for (Element link: links) {
    		// append each to the output list if it begins with "https://"
    		if (link.attr("abs:href").matches("^https?://.*$"))
	    		output.add(link.attr("abs:href"));
    	}
    	return output; 
    }

}
