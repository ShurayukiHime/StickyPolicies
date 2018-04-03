package com.example.giada.stickypoliciesapp.utilities;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.RequiresApi;
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
import java.security.spec.AlgorithmParameterSpec;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

/**
 * Created by Giada on 06/03/2018.
 */

public class CryptoUtils {

    private static KeyPair keyPair;
    private static X509Certificate certificate;
    private static SecureRandom secureRandom = new SecureRandom();
    private static final String CNUser = "Alice";
    private static final String bouncyCastleProviderName = org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME;
    private static final String androidOpenSSLProviderName = "AndroidOpenSSL";

    private static String asymmKeyGenAlg = "RSA";
    private static String asymmEncrAlg = "RSA/ECB/PKCS1Padding";
    private static String signatureAlgorithm = "MD5WithRSA";

    private static int AES_KEY_SIZE = 256;
    private static String symmKeyGenAlg = "AES";
    private static String symmEncrAlg = "AES/CBC/PKCS5Padding";

    private static String gcmEncrAlg = "AES/GCM/NoPadding";
    private static int GCM_NONCE_LENGTH = 12; // in bytes
    private static int GCM_TAG_LENGTH = 16; // in bytes

    private static String digestAlgorithm = "SHA-256";

    private final static String TAG = CryptoUtils.class.getSimpleName();

    private static void generateKeys() throws SecurityException {
        try {
            KeyPairGenerator keygen = KeyPairGenerator.getInstance(asymmKeyGenAlg, bouncyCastleProviderName);
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

        ContentSigner contentSigner = new JcaContentSignerBuilder(signatureAlgorithm).setProvider(bouncyCastleProviderName).build(keyPair.getPrivate());

        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(new X500Principal("CN="+CNUser), certSerialNumber, validityBeginDate, validityEndDate, new X500Principal("CN="+CNUser), keyPair.getPublic());

        // Basic Constraints
        BasicConstraints basicConstraints = new BasicConstraints(false); // <-- true for CA, false for EndEntity
        certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true, basicConstraints); // Basic Constraints is usually marked as critical.

        certificate = new JcaX509CertificateConverter().setProvider(bouncyCastleProviderName).getCertificate(certBuilder.build(contentSigner));
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
            Cipher c = Cipher.getInstance(asymmEncrAlg);
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
            Cipher c = Cipher.getInstance(asymmEncrAlg);
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
            keyGen = KeyGenerator.getInstance(symmKeyGenAlg);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.d(TAG, "Error in generating the symmetric random key: " + e.getMessage());
        }
        keyGen.init(AES_KEY_SIZE);
        SecretKey secretKey = keyGen.generateKey();
        return secretKey.getEncoded();
    }

    public static byte[] generateSecureIV () {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(symmEncrAlg, androidOpenSSLProviderName);
        } catch ( NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
            e.printStackTrace();
            Log.d(TAG, "Error in generating initialization vector: " + e.getMessage());
            throw new SecurityException(e.getMessage());
        }
        byte[] iv = new byte[cipher.getBlockSize()];
        secureRandom.nextBytes(iv);
        return iv;
    }

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
}
