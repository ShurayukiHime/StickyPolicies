package com.example.giada.stickypolicies.model;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public class Certificates {
	/**
     * Generate a self signed X509 certificate with Bouncy Castle.
     */
	
	private final static String CNTrustAuthority = "TrustAuthority";
	private static final String BC = org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME;
	private static String encryptionAlgorithm = "RSA";
	private static KeyPair taKeys;
	private static X509Certificate taCertificate;
	
	public static KeyPair getKeys() throws NoSuchAlgorithmException, NoSuchProviderException {
		if (taKeys == null) {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(encryptionAlgorithm, BC);
			keyPairGenerator.initialize(1024, new SecureRandom());
			taKeys = keyPairGenerator.generateKeyPair();
			return taKeys;
		} else
			return taKeys;
	}
    
   private static void generateSelfSignedX509Certificate() throws Exception {
      if (taKeys == null)
    	  getKeys();
      
       X500Name dnName = new X500Name(CNTrustAuthority);
       // Using the current timestamp as the certificate serial number
       BigInteger certSerialNumber = new BigInteger(Long.toString(System.currentTimeMillis()));
       Date validityBeginDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000); // yesterday
       Date validityEndDate = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000);  // in 1 year
       String signatureAlgorithm = "SHA256WithRSA"; // <-- Use appropriate signature algorithm based on your keyPair algorithm.

       ContentSigner contentSigner = new JcaContentSignerBuilder(signatureAlgorithm).setProvider(BC).build(taKeys.getPrivate());

       JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(dnName, certSerialNumber, validityBeginDate, validityEndDate, dnName, taKeys.getPublic());

       // Basic Constraints
       BasicConstraints basicConstraints = new BasicConstraints(true); // <-- true for CA, false for EndEntity
       certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true, basicConstraints); // Basic Constraints is usually marked as critical.

       taCertificate = new JcaX509CertificateConverter().setProvider(BC).getCertificate(certBuilder.build(contentSigner));
   }
   
   public static X509Certificate getCertificate() throws Exception {
	   if (taCertificate == null)
		   generateSelfSignedX509Certificate();
	   return taCertificate;
   }
}
