byte[] pii;
byte[] encrPiiAndIV = new byte[0];
byte[] keyAndHashEncrypted = new byte[0];
byte[] signedEncrKeyAndHash = new byte[0];
try {
	// 1) obtain policy: obtained in Bundle
	// 2) generate symmetric disposable encryption key
	byte[] encodedSymmetricKey = CryptoUtils.generateSymmetricRandomKey();
	byte[] initializationVec = CryptoUtils.generateSecureIV();
	// 3) encrypt PII
	byte[] encryptedPii = CryptoUtils.encrDecrSymmetric(Cipher.ENCRYPT_MODE, encodedSymmetricKey, initializationVec, pii);
	// 4) hash policy
	byte[] policyDigest = CryptoUtils.calculateDigest(policy.getBytes(Charset.forName("UTF-8")));
	// 5) append policy and  digest, encrypt with PubTa
	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	byteArrayOutputStream.write(encodedSymmetricKey);
	byteArrayOutputStream.write(policyDigest);
	keyAndHashEncrypted = CryptoUtils.encryptAsymmetric(taPublicKey, byteArrayOutputStream.toByteArray());
	// 6) sign
	signedEncrKeyAndHash = CryptoUtils.sign(keyAndHashEncrypted);
	
	encrPiiAndIV = new byte[initializationVec.length + encryptedPii.length];
	System.arraycopy(encryptedPii, 0, encrPiiAndIV, 0, encryptedPii.length);
	System.arraycopy(initializationVec, 0, encrPiiAndIV, encryptedPii.length, initializationVec.length);
} catch (Exception e) {
	e.printStackTrace();
	mSearchResultsTextView.setText("Sorry, some errors happened while encrypting your data.\nDon't worry, your privacy is still safe!\nPlease try again later.");
}