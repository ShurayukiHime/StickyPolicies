package com.example.giada.stickypolicies.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bouncycastle.crypto.CryptoException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.xml.sax.SAXException;

import com.example.giada.stickypolicies.model.CryptoUtilities;
import com.example.giada.stickypolicies.parser.ContentHandler_SAX;
import com.example.giada.stickypolicies.parser.XMLParserSAX;
import com.example.giada.stickypolicies.server.beans.Users;

public class DataAccessServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Users users;

	@Override
	public void init() throws ServletException {
		users = (Users) this.getServletContext().getAttribute("users");
		if (users == null) {
			users = new Users();
			this.getServletContext().setAttribute("users", users);
		}
		super.init();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();

		out.println("<html>");
		out.println("<head>");
		out.println("<title>I am a Trusted Authority</title>");
		out.println("<link rel=\"stylesheet\" href=\"styles/default.css\" type=\"text/css\"></link>");
		out.println("</head>");
		out.println("<body>");

		out.println(
				"If you are here, you have made a GET request.<br/> I receive also POST requests, in which you have to send me policies and encrypted messages to evaluate, following this pattern:<br/>");
		out.println("<br/>");
		out.println("Policy, Enc(PubTA, K||h(Policy)), Sig(PrivUser, Enc(PubTA, K||h(Policy)))");
		out.println("<br/>");
		out.println(
				"Where K is the one-time-use symmetric key, TA is me, User is the data owner.");
		out.println("<br/>");
		out.println("Do NOT send personal data, even if encrypted.");
		out.println("</body>");
		out.println("</html>");
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		BufferedReader body = request.getReader();
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = body.readLine()) != null) {
            sb.append(line).append("\n");
        } 
        body.close();
		
		JSONObject requestData = null;
		try {
			requestData = new JSONObject(sb.toString());
		} catch (JSONException e) {
			System.out.println(e.getMessage());
	    	e.printStackTrace();
		}
		System.out.println(requestData);
		
		/*
		 * NOTA BENE
		 * LA TA NON DOVREBBE RICEVERE I DATI SENSIBILI DA BOB!
		 */
		
		String policy = XML.toString(requestData.getString("policy"));
		String taEncryption = requestData.getString("taEncryption");
		String ownerSignature = requestData.getString("ownerSignature");
		String encryptedPiiString = requestData.getString("encryptedPii");
		
		byte[] keyAndHashEncrypted = Base64.getDecoder().decode(taEncryption);
		byte[] signedEncrPolicyAndHash = Base64.getDecoder().decode(ownerSignature);
		byte[] encryptedPii = Base64.getDecoder().decode(encryptedPiiString);
		
		try {
			XMLParserSAX parser = new XMLParserSAX(policy, false);
				//throws exception
				//message parsed only if everything goes well
			ContentHandler_SAX handler = parser.getHandler();
			BigInteger certificateSN = handler.getStickyPolicy().getOwner().getCertificateSN();
			users = (Users) this.getServletContext().getAttribute("users");
			X509Certificate dataOwnerCertificate = users.getCertificate(certificateSN);
			if (dataOwnerCertificate == null) {
				endConnection(response, "User with SN " + certificateSN + " non registered. Cannot provide a public key.");
				return;
			}
				// compute hash from policy
				byte[] computedPolicyDigest = CryptoUtilities.calculateDigest(policy.getBytes(Charset.forName("UTF-8")));
				// decrypt signedKeyAndHash with own private key
				byte[] computedSignedKeyAndHash = CryptoUtilities.decryptAsymmetric(
						CryptoUtilities.getKeys().getPrivate(),keyAndHashEncrypted);
				int digestLength = CryptoUtilities.getDigestSize();
				int anotherDigestLength = computedPolicyDigest.length;
				System.out.println("Two message digest sizes! " + digestLength + ", " +  anotherDigestLength); 
				// separate hash from key
				byte[] allegedSymmetricKey = Arrays.copyOfRange(computedSignedKeyAndHash, 
						0, (computedSignedKeyAndHash.length - ADIGESTLENGTH));
				byte[] retrievedPolicyDigest = Arrays.copyOfRange(computedSignedKeyAndHash, 
						(computedSignedKeyAndHash.length - ADIGESTLENGTH), ADIGESTLENGTH);
				// check if obtained hash equals computed one
				if (!(retrievedPolicyDigest.equals(computedPolicyDigest))) {
					endConnection(response, "Computed policy hash does not match with the sent one. Suspected message tampering.");
					return;
				}
				// verify user's signature
				// at this point, you trust Enc(PubTA, K||h(Policy)) i.e. keyAndHashEncrypted and computedSignedKeyAndHash, to be correct
				boolean correctTASignature = CryptoUtilities.verify(
						dataOwnerCertificate.getPublicKey(),
						signedEncrPolicyAndHash, keyAndHashEncrypted);
				// transmit alleged encrypted symmetric key
				if (!(correctTASignature)) {
					endConnection(response, "Data Owner's signature didn't match. Suspected forgery.");
					return;
				}
				PrintWriter out = response.getWriter();
				out.println(Base64.getEncoder().encodeToString(allegedSymmetricKey));
				response.setStatus(HttpServletResponse.SC_OK);
		} catch (SAXException e) {
			e.printStackTrace();
			endConnection(response, "Malformed policy document: " + e.getMessage());
		}
	}
	
	private void endConnection (HttpServletResponse response, String message) throws IOException {
		System.out.println(message);
		PrintWriter out = response.getWriter();
		out.println(message);
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
	}
}