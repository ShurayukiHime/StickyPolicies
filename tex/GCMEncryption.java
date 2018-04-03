private static final String bouncyCastleProviderName = org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME;

private static String symmKeyGenAlg = "AES";
private static int AES_KEY_SIZE = 256;
private static String gcmEncrAlg = "AES/GCM/NoPadding";
private static int GCM_NONCE_LENGTH = 12; // in bytes
private static int GCM_TAG_LENGTH = 16; // in bytes

private final static String TAG = CryptoUtils.class.getSimpleName();

public static byte[] generateSymmetricRandomKey() {
	KeyGenerator keyGen = null;
	try {
		keyGen = KeyGenerator.getInstance(symmKeyGenAlg);
	} catch (NoSuchAlgorithmException e) {
		e.printStackTrace();
		Log.d(TAG, "Error in generating the symmetric random key: " + e.getMessage());
	}
	keyGen.init(AES_KEY_SIZE);
	SecretKey secretKey = keyGen.generateKey();
	return secretKey.getEncoded();
}

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public static GCMParameterSpec getGCMParameterSpec() {
	byte[] nonce = new byte[GCM_NONCE_LENGTH];
	secureRandom.nextBytes(nonce);
	GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
	return spec;
}

@TargetApi(Build.VERSION_CODES.KITKAT)
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public static byte[] processGCM (int mode, byte[] encodedSymmKey, AlgorithmParameterSpec spec, byte[] aad, byte[] originalText) {
	SecretKeySpec skeySpec = new SecretKeySpec(encodedSymmKey, gcmEncrAlg);
	Cipher cipher = null;
	try {
		cipher = Cipher.getInstance(gcmEncrAlg, bouncyCastleProviderName);
	} catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException e) {
		e.printStackTrace();
		Log.d(TAG, "Error in symmetric GCM encryption: " + e.getMessage());
		throw new SecurityException(e.getMessage());
	}
	try {
		cipher.init(mode, skeySpec, spec);
	} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
		e.printStackTrace();
		Log.d(TAG, "Error in symmetric GCM encryption: " + e.getMessage());
		throw new SecurityException(e.getMessage());
	}
	cipher.updateAAD(aad);
	byte[] cipherText = new byte[0];
	try {
		cipherText = cipher.doFinal(originalText);
	} catch (IllegalBlockSizeException | BadPaddingException e) {
		e.printStackTrace();
		Log.d(TAG, "Error in symmetric GCM encryption: " + e.getMessage());
		throw new SecurityException(e.getMessage());
	}
	return cipherText;
}