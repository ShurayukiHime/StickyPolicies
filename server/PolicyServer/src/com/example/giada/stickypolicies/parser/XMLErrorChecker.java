package com.example.giada.stickypolicies.parser;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class XMLErrorChecker extends DefaultHandler {
	
	public void error (SAXParseException e) throws SAXException {
		System.out.println("Parsing error: "+e.getMessage());
		throw new SAXException("Parsing error: "+e.getMessage());
	}
	
	public void warning (SAXParseException e) throws SAXException { 
		System.out.println("Parsing problem: "+e.getMessage());
		throw new SAXException("Parsing problem: "+e.getMessage());
	}
	
	public void fatalError (SAXParseException e) {
		System.out.println("Parsing error: "+e.getMessage()); 
		System.out.println("Cannot continue."); 
		System.exit(1);
	}
}
