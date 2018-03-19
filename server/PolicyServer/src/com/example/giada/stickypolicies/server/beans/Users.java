package com.example.giada.stickypolicies.server.beans;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.HashMap;

public class Users implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private HashMap<BigInteger, X509Certificate> userCertificates = new HashMap<BigInteger, X509Certificate>();

	public Users() {
		super();
	}
	
	public void addCertificate(X509Certificate certificate) {
		this.userCertificates.put(certificate.getSerialNumber(), certificate);
	}
	
	public X509Certificate getCertificate (BigInteger serialNumber) {
		return this.userCertificates.get(serialNumber);
	}
	
	public String getSerialNumbers() {
		StringBuilder sb = new StringBuilder();
		for (BigInteger i : this.userCertificates.keySet())
			sb.append(i + "\n");
		return sb.toString();
	}

}
