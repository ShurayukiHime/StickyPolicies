// Setup
AsymmetricCipherKeyPair keyPair = engine.setup(64, 3);
// KeyGen
Element[] ids = engine.map(keyPair.getPublic(), "angelo", "de caro", "unisa");
CipherParameters sk0 = engine.keyGen(keyPair, ids[0]);
CipherParameters sk01 = engine.keyGen(keyPair, ids[0], ids[1]);
CipherParameters sk012 = engine.keyGen(keyPair, ids[0], ids[1], ids[2]);
// Encryption
byte[][] ciphertext0 = engine.encaps(keyPair.getPublic(), ids[0]);
byte[][] ciphertext01 = engine.encaps(keyPair.getPublic(), ids[0], ids[1]);
byte[][] ciphertext012 = engine.encaps(keyPair.getPublic(), ids[0], ids[1], ids[2]);
// Decrypt
byte[] cleartext0 = engine.decaps(sk0, ciphertext0[1]);
System.out.println(new String(cleartext0) + "");