String BC = org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME;

KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(asymmKeyGenAlg, BC);
keyPairGenerator.initialize(1024, new SecureRandom());
taKeys = keyPairGenerator.generateKeyPair();