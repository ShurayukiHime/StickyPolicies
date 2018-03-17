package com.example.giada.stickypolicies.parser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.example.giada.stickypolicies.model.PKIPolicy;

public class ContentHandler_SAX extends DefaultHandler {

	private List<String> tempData = new ArrayList<String>();
	private PKIPolicy stickyPolicy;

	private boolean isInPkiPolicy = false;
	private boolean isInPolicy = false;
	private boolean isInOwner = false;

	private boolean isInTrustedAuthority = false;
	private String[] trustedAuthority;

	private boolean isInReferenceName = false;
	private String referenceName;

	private boolean isInOwnersDetails = false;
	private String[] ownersDetails;
	
	private boolean isInSerialNumber = false;
	private BigInteger serialNumber;

	private boolean isInTarget = false;
	private String[] target;
	
	private boolean isInDataType = false;
	private String[] dataType;

	private boolean isInConstraint = false;
	private boolean constraintFlag = false;
	private String[] constraint;

	private boolean isInAction = false;
	private String[] action;

	private boolean isInValidity = false;
	private boolean isInDay = false;
	private boolean isInMonth = false;
	private boolean isInYear = false;
	private int day;
	private int month;
	private int year;
	private Date tempExpiryDate;

	public void startElement(String namespaceURI, String localName, String rawName, Attributes atts) {
		if (localName.equals("stickyPolicy")) {
			isInPkiPolicy = true;
		} else if (localName.equals("trustedAuthority")) {
			isInTrustedAuthority = true;
		} else if (localName.equals("owner")) {
			isInOwner = true;
			if (!tempData.isEmpty()) {
				trustedAuthority = new String[tempData.size()];
				tempData.toArray(trustedAuthority);
				tempData = new ArrayList<String>();
			}
		} else if (localName.equals("referenceName")) {
			isInReferenceName = true;
		} else if (localName.equals("ownersDetails")) {
			isInOwnersDetails = true;
		} else if (localName.equals("certificateSerialNumber")) {
			isInSerialNumber = true;
		} else if (localName.equals("policy")) {
			isInPolicy = true;
		} else if (localName.equals("target")) {
			isInTarget = true;
		} else if (localName.equals("dataType")) {
			isInDataType = true;
			target = new String[tempData.size()];
			tempData.toArray(target);
			tempData = new ArrayList<String>();
		} else if (localName.equals("validity")) {
			isInValidity = true;
			dataType = new String[tempData.size()];
			tempData.toArray(dataType);
			tempData = new ArrayList<String>();
		} else if (localName.equals("day")) {
			isInDay = true;
		} else if (localName.equals("month")) {
			isInMonth = true;
		} else if (localName.equals("year")) {
			isInYear = true;
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
		}
	}

	/*
	 * Questo metodo intercetta il contenuto testuale dei nodi quindi serve per
	 * aggiornare i valori in String che avevamo sopra.
	 */
	public void characters(char ch[], int start, int length) {
		if (isInPkiPolicy) {
			if (isInTrustedAuthority) {
				tempData.add(new String(ch, start, length));
			} else if (isInOwner) {
				if (isInReferenceName) {
					referenceName = new String(ch, start, length);
				} else if (isInOwnersDetails) {
					tempData.add(new String(ch, start, length));
				} else if (isInSerialNumber) {
					serialNumber = new BigInteger(new String(ch, start, length));
				}
			} else if (isInPolicy) {
				if (isInTarget) {
					tempData.add(new String(ch, start, length));
				} else if (isInDataType) {
					tempData.add(new String(ch, start, length));
				} else if (isInValidity) {
					if (isInDay)
						day = Integer.parseInt(new String(ch, start, length));
					else if (isInMonth)
						month = Integer.parseInt(new String(ch, start, length));
					else if (isInYear)
						year = Integer.parseInt(new String(ch, start, length));
				} else if (isInConstraint) {
					tempData.add(new String(ch, start, length));
				} else if (isInAction) {
					tempData.add(new String(ch, start, length));
				}
			}
		}
	}

	/*
	 * Questo metodo ci informa della fine del nodo quindi serve per aggiornare
	 * i valori boolean e per fare CONTROLLI sulle strighe che abbiamo
	 * incapsulato.
	 */
	public void endElement(String namespaceURI, String localName, String qName) {
		if (localName.equals("stickyPolicy")) {
			isInPkiPolicy = false;
		} else if (localName.equals("trustedAuthority")) {
			isInTrustedAuthority = false;
		} else if (localName.equals("referenceName")) {
			isInReferenceName = false;
		} else if (localName.equals("ownersDetails")) {
			isInOwnersDetails = false;
		} else if (localName.equals("certificateSerialNumber")) {
			isInSerialNumber = false;
			ownersDetails = new String[tempData.size()];
			tempData.toArray(ownersDetails);
			tempData = new ArrayList<String>();
		} else if (localName.equals("owner")) {
			isInOwner = false;
			
			stickyPolicy = new PKIPolicy(trustedAuthority);
			stickyPolicy.setOwner(referenceName, ownersDetails, serialNumber);
		} else if (localName.equals("target")) {
			isInTarget = false;
		} else if (localName.equals("dataType")) {
			isInDataType = false;
		} else if (localName.equals("day")) {
			isInDay = false;
		} else if (localName.equals("month")) {
			isInMonth = false;
		} else if (localName.equals("year")) {
			isInYear = false;
		} else if (localName.equals("validity")) {
			isInValidity = false;
			tempExpiryDate = new Date(year-1900, month-1, day);
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
			
			stickyPolicy.addPolicy(target, dataType, tempExpiryDate, constraint, action);
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

	public PKIPolicy getStickyPolicy() {
		return stickyPolicy;
	}

}