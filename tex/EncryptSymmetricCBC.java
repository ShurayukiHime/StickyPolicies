private static String symmetricEncrAlgorithm = "AES/CBC/PKCS5Padding";

public static byte[] encryptSymmetric(byte[] encodedSymmKey, byte[] clearText) {
	SecretKeySpec skeySpec = new SecretKeySpec(encodedSymmKey, symmetricEncrAlgorithm);
	byte[] encryptedText = new byte[0];
	try {
		Cipher cipher = Cipher.getInstance(symmetricEncrAlgorithm, "AndroidOpenSSL");
		SecureRandom secureRandom = new SecureRandom();
		byte[] iv = new byte[cipher.getBlockSize()];
		secureRandom.nextBytes(iv);
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(iv));
		encryptedText = cipher.doFinal(clearText);
	} catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchProviderException e) {
		e.printStackTrace();
		Log.d(TAG, "Error in symmetric encryption: " + e.getMessage());
		throw new SecurityException(e.getMessage());
	}
	return encryptedText;
}