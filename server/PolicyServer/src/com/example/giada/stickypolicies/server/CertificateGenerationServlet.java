package com.example.giada.stickypolicies.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.giada.stickypolicies.model.CryptoUtilities;
import com.example.giada.stickypolicies.server.beans.Users;

public class CertificateGenerationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private X509Certificate dataOwnerCert;
	private Users users;
	// TODO: manage users with java bean
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		String action = request.getParameter("action");
		if(action == null) {
			action = "";
			out.println("<html>");
			out.println("<head>");
			out.println("<title>I am a Trusted Authority</title>");
			out.println("<link rel=\"stylesheet\" href=\"styles/default.css\" type=\"text/css\"></link>");
			out.println("</head>");
			out.println("<body>");
			out.println("pwned.");
			out.println("</body>");
			out.println("</html>");
		}
		if(action.equals("obtainTAcertificate")) { 
			try {
				response.setContentType("text/plain");
				response.setCharacterEncoding("UTF-8");
				X509Certificate taCert = CryptoUtilities.getCertificate();
				String pemCert = CryptoUtilities.getPEMCertificate(taCert);
				out.println(pemCert);
				response.setStatus(HttpServletResponse.SC_OK);
			} catch (Exception e) {
				System.out.println(e.getMessage());
		    	e.printStackTrace();
		    	out.println("Oooops! Exception in sending the certificate: " + e.getMessage());
		    	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
	}

	@Override
	public void init() throws ServletException {
		users = (Users) this.getServletContext().getAttribute("users");
		if (users == null) {
			users = new Users();
			this.getServletContext().setAttribute("users", users);
		}
		super.init();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BufferedReader body = request.getReader();
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = body.readLine()) != null) {
            sb.append(line).append("\n");
        } 
        body.close();
        
        try {
			CertificateFactory fact = CertificateFactory.getInstance("X.509");
			dataOwnerCert = (X509Certificate) fact.generateCertificate(new ByteArrayInputStream(sb.toString().getBytes(Charset.forName("UTF-8"))));
			
			//controllare se users nullo
			users = (Users) this.getServletContext().getAttribute("users");
			users.addCertificate(dataOwnerCert);
			this.getServletContext().setAttribute("users", users);
		} catch (CertificateException e) {
			e.printStackTrace();
		}
		response.setStatus(HttpServletResponse.SC_OK);
	}
}
