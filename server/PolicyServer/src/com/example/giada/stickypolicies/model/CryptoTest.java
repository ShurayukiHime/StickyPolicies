package com.example.giada.stickypolicies.model;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.DigestException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;

public class CryptoTest {

	public static void main(String[] args) throws IOException {
		String fileName = "policy1.xml";
        String policyFile = null;
        
        BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            policyFile = sb.toString();
        } finally {
            br.close();
        }
        
        System.out.println(CryptoUtilities.getAlgorithmsSecProv() + "\n\n");

        byte[] policyDigest = new byte[0];
        byte[] policyDigest2 = new byte[0];
        try {
            policyDigest = CryptoUtilities.calculateDigest(policyFile.getBytes(Charset.forName("UTF-8")));
            policyDigest2 = CryptoUtilities.calculateDigest(policyFile.getBytes(Charset.forName("UTF-8")));
        } catch (DigestException e) {
            e.printStackTrace();
        }
        int digestLength = CryptoUtilities.getDigestSize();
        System.out.println("Comparing two identical digests: " + 
        		CryptoUtilities.compareDigests(policyDigest, policyDigest2));
        
        byte[] encodedSymmetricKey = CryptoUtilities.generateSymmetricRandomKey();
        int trueKeySize = encodedSymmetricKey.length;
        
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream( );
        try {
            byteArrayOutputStream.write(encodedSymmetricKey);
            byteArrayOutputStream.write(policyDigest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int sizeBefFirstEncy = byteArrayOutputStream.toByteArray().length;
        byte[] keyAndHashEncrypted = null;
        try {
        	keyAndHashEncrypted = CryptoUtilities.encryptAsymmetric(CryptoUtilities.getKeys().getPublic(),
					byteArrayOutputStream.toByteArray());
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        int sizeFirstEncr = keyAndHashEncrypted.length;

        System.out.println("Now trying to decrypt, split and compare the digests");
        byte[] computedSignedKeyAndHash = null;
		try {
			computedSignedKeyAndHash = CryptoUtilities.decryptAsymmetric(
					CryptoUtilities.getKeys().getPrivate(),keyAndHashEncrypted);
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int sizeFirstDecr =  computedSignedKeyAndHash.length;
		// separate hash from key
		byte[] allegedSymmetricKey = Arrays.copyOfRange(computedSignedKeyAndHash, 
				0, (computedSignedKeyAndHash.length - digestLength));
		int allegedKeySize = allegedSymmetricKey.length;
		byte[] retrievedPolicyDigest = Arrays.copyOfRange(computedSignedKeyAndHash, 
				(computedSignedKeyAndHash.length - digestLength), computedSignedKeyAndHash.length);
		int sizeRetrievedDigest = retrievedPolicyDigest.length;
		System.out.println("Comparing initial and decrypted digests: " + 
        		CryptoUtilities.compareDigests(policyDigest, retrievedPolicyDigest));
		System.out.println("Now trying to append the byte arrays in a different way...");
		
		byte[] secondAttempt = new byte[encodedSymmetricKey.length + policyDigest.length];
		System.arraycopy(encodedSymmetricKey, 0, secondAttempt, 0, encodedSymmetricKey.length);
		System.arraycopy(policyDigest, 0, secondAttempt, encodedSymmetricKey.length, policyDigest.length);
		int sizeBefSecondEncy = secondAttempt.length;
		
		try {
        	keyAndHashEncrypted = CryptoUtilities.encryptAsymmetric(CryptoUtilities.getKeys().getPublic(),
        			secondAttempt);
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int sizeSecondEncr = keyAndHashEncrypted.length;
		try {
			computedSignedKeyAndHash = CryptoUtilities.decryptAsymmetric(
					CryptoUtilities.getKeys().getPrivate(),keyAndHashEncrypted);
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int sizeSecondDecr = computedSignedKeyAndHash.length;
		// separate hash from key
		allegedSymmetricKey = Arrays.copyOfRange(computedSignedKeyAndHash, 
				0, (computedSignedKeyAndHash.length - digestLength));
		byte[] retrievedPolicyDigest2 = Arrays.copyOfRange(computedSignedKeyAndHash, 
				(computedSignedKeyAndHash.length - digestLength), computedSignedKeyAndHash.length);
		System.out.println("Comparing initial and decrypted digests - second attempt: " + 
        		CryptoUtilities.compareDigests(policyDigest, retrievedPolicyDigest));
		int secondAllegedKeySize = allegedSymmetricKey.length;
		int secondSizeRetrievedDigest = retrievedPolicyDigest2.length;
		
		System.out.println("\nPrinting first concat: " + Arrays.toString(byteArrayOutputStream.toByteArray()));
		System.out.println("Printing second concat: " + Arrays.toString(secondAttempt));
		System.out.println("But are they equal? " + Arrays.equals(byteArrayOutputStream.toByteArray(), secondAttempt));
		System.out.println("\nPrinting the digests after decrypt");
		System.out.println("First method: " + Arrays.toString(retrievedPolicyDigest));
		System.out.println("Second method: " + Arrays.toString(retrievedPolicyDigest2));
		System.out.println("But are they equal? " + Arrays.equals(retrievedPolicyDigest, retrievedPolicyDigest2));
		
		System.out.println("\nFIRST METHOD - Size key: " + trueKeySize + " size true digest: " + digestLength + " size before encryption: " + sizeBefFirstEncy
				+ " size encrypted key+digest: " + sizeFirstEncr + " size decrypted: " + sizeFirstDecr);
		System.out.println("Size retrieved key: " + allegedKeySize + " size retrieved digest: " + sizeRetrievedDigest);
		System.out.println("SECOND METHOD - Size key: " + trueKeySize + " size true digest: " + digestLength + " size before encryption: " + sizeBefSecondEncy
				+ " size encrypted key+digest: " + sizeSecondEncr + " size decrypted: " + sizeSecondDecr);
		System.out.println("Size retrieved key: " + secondAllegedKeySize + " size retrieved digest: " + secondSizeRetrievedDigest);

		byte[] policyEncrypted = CryptoUtilities.encryptSymmetric(encodedSymmetricKey, policyFile.getBytes(Charset.forName("UTF-8")));
		int encryptedDataSize = policyEncrypted.length;
		int cleartextSize = policyFile.getBytes(Charset.forName("UTF-8")).length;
		
		System.out.println("\nGenerated digest length: " + digestLength);
		System.out.println("Generated key length: " + trueKeySize);
		System.out.println("Asymmetric encryption of key + digest length: " + sizeFirstEncr);
		System.out.println("Cleartext size: " + cleartextSize);
		System.out.println("Encrypted data size: " + encryptedDataSize);

	}

}
