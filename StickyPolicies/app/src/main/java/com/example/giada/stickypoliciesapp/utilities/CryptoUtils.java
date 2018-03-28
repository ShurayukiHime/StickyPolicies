package com.example.giada.stickypoliciesapp.utilities;

import android.util.Log;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.DigestException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

/**
 * Created by Giada on 06/03/2018.
 */

public class CryptoUtils {

    private static KeyPair keyPair;
    private final static String CNUser = "Alice";
    private static final String BC = org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME;
    private static X509Certificate certificate;
    private static String encryptionAlgorithm = "RSA";
    private static String anotherEncryptionAlgorithm = "RSA/ECB/PKCS1Padding";
    private static String securityProvider = "BC";
    private static String signatureAlgorithm = "MD5WithRSA";
    private static String keyGenerationAlgorithm = "AES";
    private static int AES_KEY_SIZE = 256;
    private static String symmetricEncrAlgorithm = "AES/CBC/PKCS5Padding";
    private static String digestAlgorithm = "SHA-256";
    private final static String TAG = CryptoUtils.class.getSimpleName();

    private static void generateKeys() throws SecurityException {
        try {
            KeyPairGenerator keygen = KeyPairGenerator.getInstance(encryptionAlgorithm, securityProvider);
            keygen.initialize(1024);
            keyPair = keygen.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
            throw new SecurityException(e.getMessage());
        }
    }

    private static void generateSelfSignedX509Certificate() throws Exception {
        if (keyPair == null)
            generateKeys();

        // Using the current timestamp as the certificate serial number
        BigInteger certSerialNumber = new BigInteger(Long.toString(System.currentTimeMillis()));
        Date validityBeginDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000); // yesterday
        Date validityEndDate = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000);  // in 1 year
        String signatureAlgorithm = "SHA256WithRSA"; // <-- Use appropriate signature algorithm based on your keyPair algorithm.

        ContentSigner contentSigner = new JcaContentSignerBuilder(signatureAlgorithm).setProvider(BC).build(keyPair.getPrivate());

        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(new X500Principal("CN="+CNUser), certSerialNumber, validityBeginDate, validityEndDate, new X500Principal("CN="+CNUser), keyPair.getPublic());

        // Basic Constraints
        BasicConstraints basicConstraints = new BasicConstraints(false); // <-- true for CA, false for EndEntity
        certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true, basicConstraints); // Basic Constraints is usually marked as critical.

        certificate = new JcaX509CertificateConverter().setProvider(BC).getCertificate(certBuilder.build(contentSigner));
    }

    public static X509Certificate getCertificate() throws Exception {
        if (certificate == null)
            generateSelfSignedX509Certificate();
        return certificate;
    }

    public static byte[] sign (byte[] text) throws SignatureException {
        try {
            // signature
            Signature sig = Signature.getInstance(signatureAlgorithm);
            sig.initSign(keyPair.getPrivate());
            sig.update(text);
            byte[] signatureBytes = sig.sign();

            //return javax.xml.bind.DatatypeConverter.printBase64Binary(signatureBytes);
            return signatureBytes;
        } catch (Exception e) {
            e.printStackTrace();
            throw new SignatureException(e.getMessage());
        }
    }

    public static boolean verify(PublicKey pubKey, byte[] trueSignature, byte[] textToVerify) {
        try {
            Signature sig = Signature.getInstance(signatureAlgorithm);
            sig.initVerify(pubKey);
            sig.update(textToVerify);

            return sig.verify(trueSignature);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static byte[] encryptAsymmetric (Key publicKey, byte[] plaintext) {
        byte[] encodedBytes = null;
        try {
            Cipher c = Cipher.getInstance(anotherEncryptionAlgorithm);
            c.init(Cipher.ENCRYPT_MODE, publicKey);
            encodedBytes = c.doFinal(plaintext);
        } catch (Exception e) {
            Log.d(TAG, "Asymmetric encryption error: " + e.getMessage());
        }
        return encodedBytes;
    }

    public static byte[] decryptAsymmetric (Key privateKey, byte[] encodedBytes) {
        byte[] decodedBytes = null;
        try {
            Cipher c = Cipher.getInstance(anotherEncryptionAlgorithm);
            c.init(Cipher.DECRYPT_MODE, privateKey);
            decodedBytes = c.doFinal(encodedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Asymmetric decryption error: " + e.getMessage());
        }
        return decodedBytes;
    }

    public static byte[] calculateDigest(byte[] text) throws DigestException {
        try {
            MessageDigest md = MessageDigest.getInstance(digestAlgorithm);
            md.update(text);
            return md.digest();
            //return DatatypeConverter.printHexBinary(messageDigest);
        } catch (NoSuchAlgorithmException cnse) {
            throw new DigestException("Couldn't make digest of partial content " + cnse.getMessage());
        }
    }

    public static int getDigestSize() {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(digestAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Couldn't instantiate digest.");
            e.printStackTrace();
        }
        return md.getDigestLength();
    }

    public static byte[] generateSymmetricRandomKey() {
        KeyGenerator keyGen = null;
        try {
            keyGen = KeyGenerator.getInstance(keyGenerationAlgorithm);
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

    public static byte[] decryptSymmetric(byte[] encodedSymmKey, byte[] encryptedText) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(encodedSymmKey, symmetricEncrAlgorithm);
        Cipher cipher = Cipher.getInstance(symmetricEncrAlgorithm);
        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[cipher.getBlockSize()];
        secureRandom.nextBytes(iv);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(iv));
        byte[] decrypted = cipher.doFinal(encryptedText);
        return decrypted;
    }
}
