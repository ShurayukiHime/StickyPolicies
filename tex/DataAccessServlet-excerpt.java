//check if digests are equal
if(!(CryptoUtilities.compareDigests(retrievedPolicyDigest, computedPolicyDigest))) {
	endConnection(response, HttpServletResponse.SC_FORBIDDEN, "Computed policy hash does not match with the sent one. Suspected message tampering.");
	return;
}
//verify signature
boolean correctTASignature = CryptoUtilities.verify(dataOwnerCertificate.getPublicKey(), signedEncrKeyAndHash, keyAndHashEncrypted);
if(!correctTASignature) {
	endConnection(response, HttpServletResponse.SC_FORBIDDEN, "Data Owner's signature didn't match. Suspected forgery.");
	return;
}

private void endConnection (HttpServletResponse response, int responseStatusCode, String message) {
	PrintWriter out = response.getWriter();
	out.println(message);
	response.setStatus(responseStatusCode);
}