package com.example.giada.stickypolicies.model;

import java.io.IOException;
import java.io.StringWriter;
import java.security.cert.X509Certificate;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

public class Main {

	public static void main(String[] args) {
		X509Certificate taCert = null;
		try {
			taCert = Certificates.getCertificate();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		StringWriter sw = new StringWriter();
		JcaPEMWriter pw = new JcaPEMWriter(sw);
		try {
			pw.writeObject(taCert);
			pw.flush();
			String pemData = sw.toString();
			System.out.println(pemData);
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
