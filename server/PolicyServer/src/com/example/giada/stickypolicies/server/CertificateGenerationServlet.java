package com.example.giada.stickypolicies.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.cert.X509Certificate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import com.example.giada.stickypolicies.model.Certificates;

public class CertificateGenerationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private X509Certificate dataOwnerCert;
	// TODO: manage users with java bean
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<head>");
		out.println("<title>I am a Trusted Authority</title>");
		out.println("<link rel=\"stylesheet\" href=\"styles/default.css\" type=\"text/css\"></link>");
		out.println("</head>");
		out.println("<body>");
		String action = request.getParameter("action");
		if(action == null) {
			action = "";
			out.println("pwned. 2");
		}
		if(action.equals("obtainTAcertificate")) { 
			try {
				X509Certificate taCert = Certificates.getCertificate();
				out.println(Certificates.getPEMCertificate(taCert));
				//out.println("pwned. again.");
				//response.setStatus(HttpServletResponse.SC_OK);
			} catch (Exception e) {
				System.out.println(e.getMessage());
		    	e.printStackTrace();
		    	out.println("Oooops! Exception in sending the certificate: " + e.getMessage());
		    	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
		out.println("</body>");
		out.println("</html>");
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
        pr.close();
        
		response.setStatus(HttpServletResponse.SC_OK);
	}
}
