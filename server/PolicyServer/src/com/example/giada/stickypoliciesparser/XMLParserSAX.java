package main;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class XMLParserSAX extends DefaultHandler {

	/*
	 * Classe creata per essere usata da un main in cui basta solamente 
	 * generare un'istanza di XMLParserSAX e richiedere i metodi del suo handler.
	 */

	private static ContentHandler_SAX handler;

	public ContentHandler_SAX getHandler() {
		return handler;
	}

	public XMLParserSAX(String fileName) {
		String schemaFeature = "http://apache.org/xml/features/validation/schema";

		if (!fileName.endsWith(".xml")) {
			fileName += ".xml";
		}

		try {

			// 1) Costruire un parser SAX che validi il documento XML
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setValidating(true);
			spf.setNamespaceAware(true);
			SAXParser saxParser;
			saxParser = spf.newSAXParser();

			// 2) agganciare opportuni listener al lettore XML
			XMLReader xmlReader = saxParser.getXMLReader();
			XMLErrorChecker errorHandler = new XMLErrorChecker();
			xmlReader.setErrorHandler(errorHandler);

			handler = new ContentHandler_SAX();
			xmlReader.setContentHandler(handler);

			// seguente istruzione per specificare che stiamo validando tramite XML Schema 
			xmlReader.setFeature(schemaFeature,true);

			// 3) Parsificare il documento
			xmlReader.parse(fileName);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
