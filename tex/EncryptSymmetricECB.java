private static String symmetricEncrAlgorithm = "AES";
private static int AES_KEY_SIZE = 256;

public static byte[] generateSymmetricRandomKey() {
	KeyGenerator keyGen = null;
	try {
		keyGen = KeyGenerator.getInstance(symmetricEncrAlgorithm);
	} catch (NoSuchAlgorithmException e) {
		e.printStackTrace();
		Log.d(TAG, "Error in generating the symmetric random key: " + e.getMessage());
	}
	keyGen.init(AES_KEY_SIZE); 
	SecretKey secretKey = keyGen.generateKey();
	return secretKey.getEncoded();
}

public static byte[] encryptSymmetric(byte[] encodedSymmKey, byte[] clearText) {
	SecretKeySpec skeySpec = new SecretKeySpec(encodedSymmKey, symmetricEncrAlgorithm);
	byte[] encryptedText = new byte[0];
	try {
		Cipher cipher = Cipher.getInstance(symmetricEncrAlgorithm);
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		encryptedText = cipher.doFinal(clearText);
	} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
		e.printStackTrace();
		Log.d(TAG, "Error in symmetric encryption: " + e.getMessage());
	} 
	return encryptedText;
}

public static byte[] decryptSymmetric(byte[] encodedSymmKey, byte[] encryptedText) throws Exception {
	SecretKeySpec skeySpec = new SecretKeySpec(encodedSymmKey, symmetricEncrAlgorithm);
	Cipher cipher = Cipher.getInstance(symmetricEncrAlgorithm);
	cipher.init(Cipher.DECRYPT_MODE, skeySpec);
	byte[] decrypted = cipher.doFinal(encryptedText);
return decrypted;