# The implementation of Sticky Policies
Many possibilities are available, as widely presented in literature:
1. Hybrid cryptosystem, combining symmetric and asymmetric cryptography
2. Attribute-based encryption
	1. XACML language
	2. Cipertext-Policy ABE
3. Identity-based Encryption

##### Using ABE
The authors of the corresponding paper kindly developed a library, [and made it public](http://hms.isi.jhu.edu/acsc/cpabe). The CP-ABE library uses, under the curtains, the PBC library ([also available](https://crypto.stanford.edu/pbc/)), both written in the C language. This is very unlucky for a Java project.

##### Using IBE
IBE seemed also a good way to implement Sticky Policies. Looking for online libraries, it came out that it was possible to realize IBE through PBC, and a Java library for that was available: the [Java Pairing Based Cryptography Library](http://gas.dia.unisa.it/projects/jpbc/index.html).
Unfortunately it seems that this library doesn't work properly (it may as well be that I'm not skilled enough to make it work), but either way I looked for other solutions (whether reliable or not):
1. [CloudCrypto](https://github.com/liuweiran900217/CloudCrypto) - it actually offers both ABE and IBE implementation, highlighting the fact that this implementation is just a proof of concept and that there is no way to properly reconstruct the input after decryption. Written in **Java**.
2. [Proof-of-concept implementation of an identity-based encryption scheme over NTRU lattices](https://github.com/tprest/Lattice-IBE) - written in **C**.
3.  [Identity Based Encryption Java Script library](https://github.com/airpim/ibejs)
4.  [a public-key encryption in which the public key of a user is identity(unique)](https://github.com/peeyushy95/Identity-Based-Encryption) - written in **Java**.
5.  [Jpair](https://sourceforge.net/projects/jpair/) - written in **Java**.

##### Using a Hybrid Cryptosystem
This solution requires the implementation of a *Policy Enforcement Point* which is also able to use asymmetric cryptography to test the users / third parties which ask for personal data.

Moreover, this solution requires mobile entities to use both symmetric and asymmetric cryptography operations, generating one-time use symmetric keys.