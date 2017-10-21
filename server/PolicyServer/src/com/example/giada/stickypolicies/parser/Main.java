package com.example.giada.stickypolicies.parser;

public class Main {

	public static void main(String[] args) {
		System.out.print("======================== SAX ===========================\n");
		
		// Utilizzo SAX
		String fileName = "policy2";
		XMLParserSAX parser = new XMLParserSAX(fileName);
	    ContentHandler_SAX handler = parser.getHandler();
	    
		// 4) visualizzare il risultato
		System.out.println("SAX IgnorableWhitespace = " + handler.getIgnorableWhitespace());
		System.out.println("SAX pkipolicy trovate = " + handler.getStickyPolicies().size());
		System.out.println("SAX toString = " + handler.getStickyPolicies().get(1).toString());
		System.out.print("========================================================\n\n");
	}
}