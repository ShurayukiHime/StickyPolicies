package com.example.giada.stickypolicies.parser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.example.giada.stickypolicies.model.PKIPolicy;

public class ContentHandler_SAX extends DefaultHandler {

	private List<PKIPolicy> stickyPolicy = new ArrayList<PKIPolicy>();
	private List<String> tempData = new ArrayList<String>();
	private PKIPolicy temp;

	private boolean isInDataPackage = false;
	private boolean isInPkiPolicy = false;
	private boolean isInPolicy = false;
	private boolean isInOwner = false;

	private boolean isInTrustedAuthority = false;
	private String[] trustedAuthority;

	private boolean isInReferenceName = false;
	private String referenceName;

	private boolean isInOwnersDetails = false;
	private String[] ownersDetails;

	private boolean isInTarget = false;
	private String[] target;

	private boolean isInConstraint = false;
	private boolean constraintFlag = false;
	private String[] constraint;

	private boolean isInAction = false;
	private String[] action;

	private boolean isInEncryptedData = false;
	private String encryptedData;

	private boolean isInValidity = false;
	private boolean isInGiorno = false;
	private boolean isInMese = false;
	private boolean isInAnno = false;
	private int giorno;
	private int mese;
	private int anno;
	private Date tempExpiryDate;

	public void startElement(String namespaceURI, String localName, String rawName, Attributes atts) {
		if (localName.equals("dataPackage")) {
			isInDataPackage = true;
		} else if (localName.equals("pkiPolicy")) {
			isInPkiPolicy = true;
		} else if (localName.equals("trustedAuthority")) {
			isInTrustedAuthority = true;
		} else if (localName.equals("owner")) {
			isInOwner = true;
			// inizializziamo pkipolicy qui perch� ci piacciono le cose fatte
			// male *thumbs up*
			trustedAuthority = new String[tempData.size()];
			tempData.toArray(trustedAuthority);
			tempData = new ArrayList<String>();
		} else if (localName.equals("referenceName")) {
			isInReferenceName = true;
		} else if (localName.equals("ownersDetails")) {
			isInOwnersDetails = true;
		} else if (localName.equals("policy")) {
			isInPolicy = true;
		} else if (localName.equals("target")) {
			isInTarget = true;
		} else if (localName.equals("validity")) {
			isInValidity = true;
			target = new String[tempData.size()];
			tempData.toArray(target);
			tempData = new ArrayList<String>();
		} else if (localName.equals("giorno")) {
			isInGiorno = true;
		} else if (localName.equals("mese")) {
			isInMese = true;
		} else if (localName.equals("anno")) {
			isInAnno = true;
		} else if (localName.equals("constraint")) {
			isInConstraint = true;
		} else if (localName.equals("action")) {
			isInAction = true;
			if (constraintFlag) {
				constraint = new String[tempData.size()];
				tempData.toArray(constraint);
				tempData = new ArrayList<String>();
				constraintFlag = false;
			}
		} else if (localName.equals("encryptedData")) {
			isInEncryptedData = true;
		}
	}

	/*
	 * Questo metodo intercetta il contenuto testuale dei nodi quindi serve per
	 * aggiornare i valori in String che avevamo sopra.
	 */
	public void characters(char ch[], int start, int length) {
		if (isInPkiPolicy && isInDataPackage) {
			if (isInTrustedAuthority) {
				tempData.add(new String(ch, start, length));
			} else if (isInOwner) {
				if (isInReferenceName) {
					referenceName = new String(ch, start, length);
				} else if (isInOwnersDetails) {
					tempData.add(new String(ch, start, length));
				}
			} else if (isInPolicy) {
				if (isInTarget) {
					tempData.add(new String(ch, start, length));
				} else if (isInValidity) {
					if (isInGiorno)
						giorno = Integer.parseInt(new String(ch, start, length));
					else if (isInMese)
						mese = Integer.parseInt(new String(ch, start, length));
					else if (isInAnno)
						anno = Integer.parseInt(new String(ch, start, length));
				} else if (isInConstraint) {
					tempData.add(new String(ch, start, length));
				} else if (isInAction) {
					tempData.add(new String(ch, start, length));
				}
			}
		} else if (isInEncryptedData) {
			encryptedData = new String(ch, start, length);
		}
	}

	/*
	 * Questo metodo ci informa della fine del nodo quindi serve per aggiornare
	 * i valori boolean e per fare CONTROLLI sulle strighe che abbiamo
	 * incapsulato.
	 */
	public void endElement(String namespaceURI, String localName, String qName) {
		if (localName.equals("dataPackage")) {
			isInDataPackage = false;
			stickyPolicy.add(temp);
		} else if (localName.equals("pkiPolicy")) {
			isInPkiPolicy = false;
		} else if (localName.equals("trustedAuthority")) {
			isInTrustedAuthority = false;
		} else if (localName.equals("referenceName")) {
			isInReferenceName = false;
		} else if (localName.equals("ownersDetails")) {
			isInOwnersDetails = false;
		} else if (localName.equals("owner")) {
			isInOwner = false;
			ownersDetails = new String[tempData.size()];
			tempData.toArray(ownersDetails);
			tempData = new ArrayList<String>();
			
			temp = new PKIPolicy(trustedAuthority, encryptedData);
			temp.setOwner(referenceName, ownersDetails);
		} else if (localName.equals("target")) {
			isInTarget = false;
		} else if (localName.equals("giorno")) {
			isInGiorno = false;
		} else if (localName.equals("mese")) {
			isInMese = false;
		} else if (localName.equals("anno")) {
			isInAnno = false;
		} else if (localName.equals("validity")) {
			isInValidity = false;
			tempExpiryDate = new Date(anno-1900, mese-1, giorno);
		} else if (localName.equals("constraint")) {
			isInConstraint = false;
			constraintFlag = true;
		} else if (localName.equals("action")) {
			isInAction = false;
		} else if (localName.equals("policy")) {
			isInPolicy = false;
			action = new String[tempData.size()];
			tempData.toArray(action);
			tempData = new ArrayList<String>();
			
			temp.addPolicy(target, tempExpiryDate, constraint, action);
		} else if (localName.equals("encryptedData")) {
			isInEncryptedData = false;
		}
	}

	/*
	 * da qui in poi si utilizzano i metodi per restituire ciò che serve.
	 * Sopra, i valori che dichiariamo sotto, vengono aggiornati ad ogni
	 * iterazione quindi conviene dichiarare sotto ciò che serve, come vettori
	 * di stringhe, interi ecc e poi restituirli banalmente.
	 */

	private int ignorableWhitespace = 0;

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		ignorableWhitespace += length;
	}

	public int getIgnorableWhitespace() {
		return ignorableWhitespace;
	}

	public List<PKIPolicy> getStickyPolicies() {
		return stickyPolicy;
	}

}