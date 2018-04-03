private static String symmEncrAlg = "AES/CBC/PKCS5Padding";
private static final String androidOpenSSLProviderName = "AndroidOpenSSL";

public static byte[] encrDecrSymmetric(int cipherMode, byte[] encodedSymmKey, byte[] initializationVector, byte[] clearText) {
	SecretKeySpec skeySpec = new SecretKeySpec(encodedSymmKey, symmEncrAlg);
	byte[] encryptedText = new byte[0];
	try {
		Cipher cipher = Cipher.getInstance(symmEncrAlg, androidOpenSSLProviderName);
		cipher.init(cipherMode, skeySpec, new IvParameterSpec(initializationVector));
		encryptedText = cipher.doFinal(clearText);
	} catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchProviderException e) {
		e.printStackTrace();
		if (cipherMode == Cipher.ENCRYPT_MODE)
			Log.d(TAG, "Error in symmetric encryption: " + e.getMessage());
		if (cipherMode == Cipher.DECRYPT_MODE)
			Log.d(TAG, "Error in symmetric decryption: " + e.getMessage());
		throw new SecurityException(e.getMessage());
	}
	return encryptedText;
}