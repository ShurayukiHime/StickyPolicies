package com.example.giada.stickypolicies.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.DigestException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xml.sax.SAXException;

import com.example.giada.stickypolicies.model.CryptoUtilities;
import com.example.giada.stickypolicies.parser.ContentHandler_SAX;
import com.example.giada.stickypolicies.parser.XMLParserSAX;
import com.example.giada.stickypolicies.server.beans.Users;
import com.google.gson.Gson;

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
		
		/*
		 * NOTA BENE
		 * LA TA NON DOVREBBE RICEVERE I DATI SENSIBILI DA BOB!
		 */
        
        Gson requestData = new Gson();
        EncryptedData data = requestData.fromJson(sb.toString(), EncryptedData.class);
        String policy = data.getStickyPolicy();
        byte[] keyAndHashEncrypted = data.getKeyAndHashEncrypted();
        byte[] signedEncrKeyAndHash = data.getSignedEncrkeyAndHash();
		try {
			XMLParserSAX parser = new XMLParserSAX(policy, false);
				//throws exception
				//message parsed only if everything goes well
			ContentHandler_SAX handler = parser.getHandler();
			BigInteger certificateSN = handler.getStickyPolicy().getOwner().getCertificateSN();
			users = (Users) this.getServletContext().getAttribute("users");
			X509Certificate dataOwnerCertificate = users.getCertificate(certificateSN);
			if (dataOwnerCertificate == null) {
				endConnection(response, HttpServletResponse.SC_FORBIDDEN, "User with SN " + certificateSN + " not registered. Cannot provide a public key.");
				return;
			}
				// compute hash from policy
				byte[] computedPolicyDigest = CryptoUtilities.calculateDigest(policy.getBytes(Charset.forName("UTF-8")));
				// decrypt signedKeyAndHash with own private key
				byte[] computedSignedKeyAndHash = CryptoUtilities.encrDecrAsymmetric(Cipher.DECRYPT_MODE,
						CryptoUtilities.getKeys().getPrivate(),keyAndHashEncrypted);
				int digestLength = CryptoUtilities.getDigestSize();
				// separate hash from key
				byte[] allegedSymmetricKey = Arrays.copyOfRange(computedSignedKeyAndHash, 
						0, (computedSignedKeyAndHash.length - digestLength));
				byte[] retrievedPolicyDigest = Arrays.copyOfRange(computedSignedKeyAndHash, 
						(computedSignedKeyAndHash.length - digestLength), computedSignedKeyAndHash.length);
				// check if obtained hash equals computed one
				if (!(CryptoUtilities.compareDigests(retrievedPolicyDigest, computedPolicyDigest))) {
					endConnection(response, HttpServletResponse.SC_FORBIDDEN, "Computed policy hash does not match with the sent one. Suspected message tampering.");
					return;
				}
				// verify user's signature
				// at this point, you trust Enc(PubTA, K||h(Policy)) i.e. keyAndHashEncrypted and computedSignedKeyAndHash, to be correct
				boolean correctTASignature = CryptoUtilities.verify(
						dataOwnerCertificate.getPublicKey(),
						signedEncrKeyAndHash, keyAndHashEncrypted);
				// transmit alleged encrypted symmetric key
				if (!correctTASignature) {
					endConnection(response, HttpServletResponse.SC_FORBIDDEN, "Data Owner's signature didn't match. Suspected forgery.");
					return;
				}
				PrintWriter out = response.getWriter();
				response.setContentType("application/json; charset=utf-8");
				Gson gson = new Gson();
				out.println(gson.toJson(allegedSymmetricKey, byte[].class));
				response.setStatus(HttpServletResponse.SC_OK);
		} catch (SAXException e) {
			e.printStackTrace();
			endConnection(response, HttpServletResponse.SC_FORBIDDEN, "Malformed policy document: " + e.getMessage());
		} catch (DigestException | NoSuchAlgorithmException | NoSuchProviderException e) {
			e.printStackTrace();
			endConnection(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error in performing cryptographic operations: " + e.getMessage());
		} 
	}
	
	private class EncryptedData {
        private String stickyPolicy;
        private byte[] keyAndHashEncrypted;
        private byte[] signedEncrkeyAndHash;
        private byte[] encryptedPii;

        public EncryptedData() {
        }

        public EncryptedData(String stickyPolicy, byte[] keyAndHashEncrypted, byte[] signedEncrkeyAndHash, byte[] encryptedPii) {
            this.stickyPolicy = stickyPolicy;
            this.keyAndHashEncrypted = keyAndHashEncrypted;
            this.signedEncrkeyAndHash = signedEncrkeyAndHash;
            this.encryptedPii = encryptedPii;
        }

        public String getStickyPolicy() {
            return stickyPolicy;
        }

        public void setStickyPolicy(String stickyPolicy) {
            this.stickyPolicy = stickyPolicy;
        }

        public byte[] getKeyAndHashEncrypted() {
            return keyAndHashEncrypted;
        }

        public void setKeyAndHashEncrypted(byte[] keyAndHashEncrypted) {
            this.keyAndHashEncrypted = keyAndHashEncrypted;
        }

        public byte[] getSignedEncrkeyAndHash() {
            return signedEncrkeyAndHash;
        }

        public void setSignedEncrkeyAndHash(byte[] signedEncrkeyAndHash) {
            this.signedEncrkeyAndHash = signedEncrkeyAndHash;
        }

        public byte[] getEncryptedPii() {
            return encryptedPii;
        }

        public void setEncryptedPii(byte[] encryptedPii) {
            this.encryptedPii = encryptedPii;
        }
    }
	
	private void endConnection (HttpServletResponse response, int responseStatusCode, String message) throws IOException {
		System.out.println(message);
		PrintWriter out = response.getWriter();
		out.println(message);
		response.setStatus(responseStatusCode);
	}
}