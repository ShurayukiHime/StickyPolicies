package com.example.giada.stickypolicies.parser;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class XMLParserSAX extends DefaultHandler {
	private static ContentHandler_SAX handler;
	private static final String XSDdocument = "PKIstickypolicy.xsd"; 
	private static final String xmlns = "http://www.w3.org/2001/XMLSchema";
		// can be hardcoded since its content are known a priori and won't change

	public ContentHandler_SAX getHandler() {
		return handler;
	}

	public XMLParserSAX(String fileName, boolean fromFile) throws SAXException {
		String schemaFeature = "http://apache.org/xml/features/validation/schema";
		try {
			// 1) build up a sax parser to validate the xml document
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setValidating(true);
			spf.setNamespaceAware(true);
			SAXParser saxParser;
			saxParser = spf.newSAXParser();

			// 2) hook listeners to xml reader
			XMLReader xmlReader = saxParser.getXMLReader();
			XMLErrorChecker errorHandler = new XMLErrorChecker();
			xmlReader.setErrorHandler(errorHandler);

			handler = new ContentHandler_SAX();
			xmlReader.setContentHandler(handler);

			// specify we are using xml schema
			xmlReader.setFeature(schemaFeature, true);

			if (fromFile) {
				if (!fileName.endsWith(".xml"))
					fileName += ".xml";
				// 3) parse document
				xmlReader.parse(fileName);
			} else {
				xmlReader.parse(new InputSource(new StringReader(fileName)));
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			throw new SAXException(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void isValidXML (String document) throws SAXException {
		InputStream is = new ByteArrayInputStream(document.getBytes(Charset.forName("UTF-8")));
		try {
			InputSource isrc = new InputSource(new FileInputStream(XSDdocument));
			SAXSource sourceXSD = new SAXSource(isrc);
			SchemaFactory.newInstance(xmlns).newSchema(sourceXSD).newValidator()
					.validate(new StreamSource(is));
		} catch (SAXException | IOException e) {
			e.printStackTrace();
			throw new SAXException(e.getMessage());
		}
	}
}
