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

public static byte[] encrDecrSymmetric(int cipherMode, byte[] encodedSymmKey, byte[] originalText) {
	SecretKeySpec skeySpec = new SecretKeySpec(encodedSymmKey, symmetricEncrAlgorithm);
	byte[] processedText = new byte[0];
	try {
		Cipher cipher = Cipher.getInstance(symmetricEncrAlgorithm);
		cipher.init(cipherMode, skeySpec);
		processedText = cipher.doFinal(originalText);
	} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
		e.printStackTrace();
		if (cipherMode == Cipher.ENCRYPT_MODE)
			Log.d(TAG, "Error in symmetric encryption: " + e.getMessage());
		if (cipherMode == Cipher.DECRYPT_MODE)
			Log.d(TAG, "Error in symmetric decryption: " + e.getMessage());
		throw new SecurityException(e.getMessage());
	} 
	return processedText;
}