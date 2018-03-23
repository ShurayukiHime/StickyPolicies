package it.unisa.dia.gas.crypto.jpbc.cipher;

import {...}

public abstract class PairingAsymmetricBlockCipher implements AsymmetricBlockCipher {

	// some other fields and functions...

    /* Process a single block using the basic cipher algorithm.
     *
     * @param in    the input array.
     * @param inOff the offset into the input buffer where the data starts.
     * @param inLen the length of the data to be processed.
     * @return the result of the cipher process.
     * @throws org.bouncycastle.crypto.DataLengthException the input block is too large.
     */
    public byte[] processBlock(byte[] in, int inOff, int inLen) throws InvalidCipherTextException {
        if (key == null)
            throw new IllegalStateException("Engine not initialized");

        int maxLength = getInputBlockSize();

        if (inLen < maxLength) {
          System.out.println("inLen: " +inLen + "; maxLength: " + maxLength);
            throw new DataLengthException("Input too small for the cipher.");
        }
        return process(in, inOff, inLen);
    }