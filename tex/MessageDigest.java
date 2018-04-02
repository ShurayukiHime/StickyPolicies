import java.security.MessageDigest;
import java.security.DigestException;
import java.security.NoSuchAlgorithmException;

	public static byte[] calculateDigest(byte[] text) throws DigestException {
        try {
            MessageDigest md = MessageDigest.getInstance(digestAlgorithm);
            md.update(text);
            return md.digest();
        } catch (NoSuchAlgorithmException cnse) {
            throw new DigestException("Couldn't make digest of partial content " + cnse.getMessage());
        }
}