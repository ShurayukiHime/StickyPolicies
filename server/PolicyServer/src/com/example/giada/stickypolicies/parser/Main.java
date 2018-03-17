package com.example.giada.stickypolicies.parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.xml.sax.SAXException;

public class Main {

	public static void main(String[] args) {
		System.out.print("======================== SAX ===========================\n");
		
		String fileName = "policy1";
		String everything = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName + ".xml"));
			try {
			    StringBuilder sb = new StringBuilder();
			    String line = br.readLine();

			    while (line != null) {
			        sb.append(line);
			        sb.append(System.lineSeparator());
			        line = br.readLine();
			    }
			    everything = sb.toString();
			} finally {
			    br.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		XMLParserSAX parser = null;
		try {
			parser = new XMLParserSAX(fileName, true);
			//parser = new XMLParserSAX(everything, false);
		} catch (SAXException e) {
			e.printStackTrace();
		}
	    ContentHandler_SAX handler = parser.getHandler();
	    
		System.out.println("SAX toString = " + handler.getStickyPolicy().toString());
		System.out.print("========================================================\n\n");
	}
}