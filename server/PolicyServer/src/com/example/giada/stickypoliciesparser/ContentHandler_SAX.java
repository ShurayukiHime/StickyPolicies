package com.example.giada.stickypoliciesparser;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler; 

public class ContentHandler_SAX extends DefaultHandler {
	
	/*
	 * Questi valori servono per capire se si è in quel determinato tag "inTag"
	 * e servono per incapsulare il valore del tag.
	 */
	private boolean isInTrustedAuthority = false;
	private String TrustedAuthority;

	private boolean isInReferenceName = false;
	private String referenceName;

	private boolean isInOwnersDetails = false;
	private String ownersDetails;

	private boolean isInTarget = false;
	private String target;
	
	private boolean isInConstraint = false;
	private String constraint;
	
	private boolean isInAction = false;
	private String action;
	
	private boolean isInEncryptedData = false;
	private String encryptedData;

	private boolean isInValidity = false;
	private boolean isInGiorno = false;
	private boolean isInMese = false;
	private boolean isInAnno = false;
	private int giorno;
	private int mese;
	private int anno;
	
	boolean trovatoTarget = false;
	
	/*
	 * Questo metodo si basa sul "localname" e se ci si trova nei nodi che ci interessano, 
	 * aggiorniamo i valori boolean che ci dicono se siamo in quel determinato nodo e poi 
	 * incrementiamo i valori che dobbiamo restituire.
	 * 
	 * Ci serve solo quando basta capire se si è in quel nodo, quindi magari per contatori
	 */
	public void startElement (String namespaceURI, String localName, String rawName, Attributes atts) { 
		//System.out.println("AddressListContentHandler.startElement   namespaceURI=" + namespaceURI + " localName=" + localName + " rawName=" +rawName +" atts="+atts);
		if (localName.equals("entry")){
			peopleAmount++;
			nomeProprio = null;
			cognome = null;
		}
		else if(localName.equals("NomeProprio")){
			inNomeProprio = true;
		}
		else if(localName.equals("Cognome")){
			inCognome = true;
		}
		else if(localName.equals("Telefono")){
			inTelefono = true;
		}
	} 

	
	/*
	 * Questo metodo intercetta il contenuto testuale dei nodi quindi serve per aggiornare
	 *  i valori in String che avevamo sopra.
	 */
	public void characters (char ch[], int start, int length) {
		//System.out.println("AddressListContentHandler.characters   start=" + start + " length=" + length + " ch=" +new String(ch,start,length));
		if( inNomeProprio ){
			nomeProprio = new String(ch,start,length);
		}
		else if( inCognome ){
			cognome = new String(ch,start,length);
		}
		else if( inTelefono ){
			telefono = new String(ch,start,length);
		}
	} 

	/*
	 * Questo metodo ci informa della fine del nodo quindi serve per aggiornare i
	 * valori boolean e per fare CONTROLLI sulle strighe che abbiamo incapsulato.
	 */
	public void endElement(String namespaceURI, String localName, String qName) {
		//System.out.println("AddressListContentHandler.endElement   namespaceURI=" + namespaceURI + " localName=" + localName + " qName=" +qName);
		if(localName.equals("NomeProprio")){
			inNomeProprio = false;
		}
		else if(localName.equals("Cognome")){
			inCognome = false;
		}
		else if(localName.equals("Telefono")){
			inTelefono = false;
			if( nomeProprio.startsWith("Don")){
				donTel.addElement(telefono);
			}
		}
		else if (localName.equals("entry")){
			if( !trovatoTarget && nomeProprio!=null && cognome!=null ){ // controllo non necessario per documenti XML validi
				if( nomeProprio.equals("Mickey") && cognome.equals("Mouse") ){
					trovatoTarget = true;
				}
				else{
					peoplePreMM++;
				}
			}
		}
	}
	
	/*
	 * da qui in poi si utilizzano i metodi per restituire ciò che serve. 
	 * Sopra, i valori che dichiariamo sotto, vengono aggiornati ad ogni iterazione
	 * quindi conviene dichiarare sotto ciò che serve, come vettori di stringhe, interi
	 * ecc e poi restituirli banalmente. 
	 */
	
	private int ignorableWhitespace = 0;
	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		ignorableWhitespace += length;
	}
	public int getIgnorableWhitespace(){
		return ignorableWhitespace;
	}
	
	private int peopleAmount = 0;
	public int getPeopleAmount(){
		return peopleAmount;
	}
	
	private int peoplePreMM = 0;
	public int getPeoplePreMM(){
		return peoplePreMM;
	}

	private Vector<String> donTel = new Vector<String>();
	public Vector<String> getDonTel(){
		return donTel;
	}
	
}