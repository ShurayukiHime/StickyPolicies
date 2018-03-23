package it.unisa.dia.gas.crypto.jpbc.kem;

import {...}

public abstract class PairingKeyEncapsulationMechanism extends PairingAsymmetricBlockCipher implements KeyEncapsulationMechanism {

    private static byte[] EMPTY = new byte[0];
	
	// some other functions...
	
	public byte[] processBlock(byte[] in) throws InvalidCipherTextException {
        return processBlock(in, in.length, 0);
    }
	
	public byte[] process() throws InvalidCipherTextException {
        return processBlock(EMPTY, 0, 0);
    }
}