package it.unisa.dia.gas.crypto.jpbc.kem;

import it.unisa.dia.gas.crypto.jpbc.cipher.PairingAsymmetricBlockCipher;
import it.unisa.dia.gas.crypto.kem.KeyEncapsulationMechanism;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.bouncycastle.crypto.InvalidCipherTextException;

/**
 * @author Angelo De Caro (jpbclib@gmail.com)
 */
public abstract class PairingKeyEncapsulationMechanism extends PairingAsymmetricBlockCipher
		implements KeyEncapsulationMechanism {

	private static byte[] EMPTY = new byte[0];

	protected int keyBytes = 0;

	public int getKeyBlockSize() {
		return keyBytes;
	}

	public int getInputBlockSize() {
		if (forEncryption)
			return 0;

		return outBytes - keyBytes;
	}

	public int getOutputBlockSize() {
		if (forEncryption)
			return outBytes;

		return keyBytes;
	}

	public byte[] processBlock(byte[] in) throws InvalidCipherTextException {
		//codice originale -- non funziona, fa partire l'offset dalla fine...
		//return processBlock(in, in.length, 0);
		return processBlock (in, 0, in.length);
	}

	public byte[] process() throws InvalidCipherTextException {
    	/*
			If you have a small-ish file and you would like to read its
			entire contents in one pass, you can use the readAllBytes(Path)
			or readAllLines(Path, Charset) method. These methods take care
			of most of the work for you, such as opening and closing the
			stream, but are not intended for handling large files. The
			following code shows how to use the readAllBytes method:
    	*/
    	Path file = Paths.get(System.getProperty("user.home"),"Desktop", "jpbc-crypto", "somepersonaldata.txt");
		System.out.println(" " + System.getProperty("user.dir"));
		System.out.println(" " + file.toString());
    	byte[] fileArray;
    	try {
			fileArray = Files.readAllBytes(file);
		} catch (IOException e) {
			e.printStackTrace();
			return processBlock(EMPTY, 0, 0);
		}
    	System.out.println("ProcessBlock called w/ fileArray, 0, " + fileArray.length);
        return processBlock(fileArray, 0, fileArray.length);
    }

}
