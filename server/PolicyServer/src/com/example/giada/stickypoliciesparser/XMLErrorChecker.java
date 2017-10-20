package com.example.giada.stickypoliciesparser;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class XMLErrorChecker extends DefaultHandler {
	
	public void error (SAXParseException e) {
		System.out.println("Parsing error: "+e.getMessage()); 
	}
	
	public void warning (SAXParseException e) { 
		System.out.println("Parsing problem: "+e.getMessage());
	}
	
	public void fatalError (SAXParseException e) {
		System.out.println("Parsing error: "+e.getMessage()); 
		System.out.println("Cannot continue."); 
		System.exit(1);
	}
}
