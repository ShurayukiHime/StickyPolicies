package com.example.giada.stickypolicies.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.giada.stickypolicies.model.Certificates;

public class CertificateGenerationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private X509Certificate dataOwnerCert;
	// TODO: manage users with java bean
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		String action = request.getParameter("action");
		if(action == null)
			action = "";
		if(action.equals("obtainTAcertificate")) { 
			try {
				X509Certificate taCert = Certificates.getCertificate();
				//response.getOutputStream().write(taCert.getEncoded());
				// sends the certificate with DER format.
				// PEM was preferred because the certificate is sent as a string and it is
				// easier to receive it client - side
				StringWriter sw = new StringWriter();
				JcaPEMWriter pw = new JcaPEMWriter(sw);
				pw.writeObject(taCert);
				pw.flush();
				String pemData = sw.toString();
				out.println(pemData);
				pw.close();
			} catch (Exception e) {
				System.out.println(e.getMessage());
		    	e.printStackTrace();
		    	out.println("Oooops! Exception in sending the certificate: " + e.getMessage());;
			}
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BufferedReader body = request.getReader();
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = body.readLine()) != null) {
            sb.append(line).append("\n");
        } 
        body.close();
        
        PEMParser pr = new PEMParser(new StringReader(line));
        dataOwnerCert = (X509Certificate) pr.readObject();
        
		response.setStatus(HttpServletResponse.SC_OK);
	}
}
