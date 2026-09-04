package bank.management.system;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Utility class for secure PIN hashing using PBKDF2WithHmacSHA256.
 *
 * The salt is derived deterministically from the card number so that the
 * resulting hash can still be used as a SQL lookup key (WHERE pin = ?).
 * The same card number + PIN always produces the same hash.
 */
public class PinHasher {

    private static final String SALT_PREFIX = "BankMgmt_";
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    /**
     * Hash a plaintext PIN using PBKDF2 with a salt derived from the card number.
     *
     * @param pin        the plaintext 4-digit PIN entered by the user
     * @param cardNumber the card number (used to derive a deterministic salt)
     * @return Base64-encoded hash string
     */
    public static String hashPin(String pin, String cardNumber) {
        try {
            String salt = SALT_PREFIX + normalizeCardNumber(cardNumber);
            byte[] saltBytes = salt.getBytes("UTF-8");

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(pin.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH);

            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash PIN", e);
        }
    }

    /**
     * Verify a plaintext PIN against a stored hash.
     *
     * @param pin        the plaintext PIN to check
     * @param cardNumber the card number
     * @param storedHash the hash stored in the database
     * @return true if the PIN matches the stored hash
     */
    public static boolean verifyPin(String pin, String cardNumber, String storedHash) {
        String computedHash = hashPin(pin, cardNumber);
        return computedHash.equals(storedHash);
    }

    /**
     * Normalize the card number by removing dashes and whitespace.
     * This ensures consistent hashing whether the user types dashes or not.
     */
    private static String normalizeCardNumber(String cardNumber) {
        if (cardNumber == null) return "";
        return cardNumber.replaceAll("[-\\s]", "");
    }
}
