package main;

import org.w3c.dom.*;

public class Main {

	public static void main(String[] args) {
		System.out.print("======================== SAX ===========================\n");
		
		// Utilizzo SAX
		String fileName = "addressList";
		XMLParserSAX parser = new XMLParserSAX(fileName);
	    ContentHandler_SAX handler = parser.getHandler();
	    
		// 4) visualizzare il risultato
		System.out.println("SAX IgnorableWhitespace = " + handler.getIgnorableWhitespace());
		System.out.println("SAX PeopleAmount = " + handler.getPeopleAmount());
		System.out.println("SAX PeoplePreMM = " + handler.getPeoplePreMM());
		System.out.println("SAX DonTel = " + handler.getDonTel());
		System.out.print("========================================================\n\n");
	}