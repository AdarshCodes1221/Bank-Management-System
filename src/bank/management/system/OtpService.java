package bank.management.system;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.nio.charset.StandardCharsets;

/**
 * OTP (One-Time Password) service for secure authentication of sensitive operations.
 *
 * Features:
 * - Generates cryptographically secure 6-digit OTPs using SecureRandom
 * - Stores only a SHA-256 hash of the OTP in memory (never plaintext)
 * - OTPs expire after 5 minutes
 * - Maximum of 3 verification attempts per OTP
 * - Thread-safe using ConcurrentHashMap
 *
 * Usage flow:
 * 1. Call generateAndSendOtp(pinHash) to create and email an OTP
 * 2. Call validateOtp(pinHash, enteredOtp) to verify the user's input
 */
public class OtpService {

    public enum OtpStatus {
        VALID,
        INVALID,            // Wrong OTP, attempts remaining
        EXPIRED,            // OTP exceeded 5-minute lifetime
        ATTEMPTS_EXHAUSTED, // 3 failed attempts, OTP invalidated
        NOT_FOUND           // No OTP generated for this key
    }

    private static final int OTP_DIGITS = 6;
    private static final int OTP_EXPIRY_MS = 5 * 60 * 1000; // 5 minutes
    private static final int MAX_ATTEMPTS = 3;

    private static OtpService instance;

    // Thread-safe in-memory store: pinHash → OtpRecord
    private final ConcurrentMap<String, OtpRecord> otpStore = new ConcurrentHashMap<>();

    private final EmailService emailService;

    private OtpService() {
        this.emailService = new EmailService();
    }

    public static synchronized OtpService getInstance() {
        if (instance == null) {
            instance = new OtpService();
        }
        return instance;
    }

    public boolean isEmailConfigured() {
        return emailService.isConfigured();
    }

    /**
     * Generates a secure OTP, stores its hash, and emails it to the user.
     *
     * @param pinHash the hashed PIN used to look up the user's email
     * @return true if the OTP was generated and emailed successfully
     */
    public boolean generateAndSendOtp(String pinHash) {
        if (pinHash == null) {
            return false;
        }

        // Look up registered email by PIN hash
        String email = lookupEmailByPinHash(pinHash);
        if (email == null) {
            return false;
        }

        // Generate cryptographically secure 6-digit OTP
        String otp = generateSecureOtp();

        // Hash the OTP for secure storage (SHA-256)
        String otpHash = hashOtp(otp);

        // Store with expiry and attempt counter
        OtpRecord record = new OtpRecord();
        record.otpHash = otpHash;
        record.expiryTime = System.currentTimeMillis() + OTP_EXPIRY_MS;
        record.attemptsLeft = MAX_ATTEMPTS;

        otpStore.put(pinHash, record);

        // Send OTP via email
        boolean sent = emailService.sendOtpEmail(email, otp);
        if (!sent) {
            // Clean up: remove the stored OTP if email failed
            otpStore.remove(pinHash);
            return false;
        }

        return true;
    }

    /**
     * Validates the user-entered OTP against the stored hash.
     *
     * @param pinHash     the hashed PIN used as the lookup key
     * @param enteredOtp  the OTP string entered by the user
     * @return the validation status
     */
    public OtpStatus validateOtp(String pinHash, String enteredOtp) {
        if (pinHash == null || enteredOtp == null || enteredOtp.trim().isEmpty()) {
            return OtpStatus.NOT_FOUND;
        }

        OtpRecord record = otpStore.get(pinHash);
        if (record == null) {
            return OtpStatus.NOT_FOUND;
        }

        // Check expiry
        if (System.currentTimeMillis() > record.expiryTime) {
            otpStore.remove(pinHash);
            return OtpStatus.EXPIRED;
        }

        // Check attempts
        if (record.attemptsLeft <= 0) {
            otpStore.remove(pinHash);
            return OtpStatus.ATTEMPTS_EXHAUSTED;
        }

        // Hash the entered OTP and compare
        String enteredHash = hashOtp(enteredOtp.trim());
        if (record.otpHash.equals(enteredHash)) {
            // OTP verified — consume it (single-use)
            otpStore.remove(pinHash);
            return OtpStatus.VALID;
        }

        // Wrong OTP — decrement attempts
        record.attemptsLeft--;
        if (record.attemptsLeft <= 0) {
            otpStore.remove(pinHash);
            return OtpStatus.ATTEMPTS_EXHAUSTED;
        }

        return OtpStatus.INVALID;
    }

    /**
     * Returns the number of remaining attempts for the current OTP.
     */
    public int getRemainingAttempts(String pinHash) {
        OtpRecord record = otpStore.get(pinHash);
        return (record != null) ? record.attemptsLeft : 0;
    }

    /**
     * Returns a formatted countdown string (MM:SS) for the current OTP.
     */
    public String getOtpCountdown(String pinHash) {
        OtpRecord record = otpStore.get(pinHash);
        if (record != null) {
            long remaining = record.expiryTime - System.currentTimeMillis();
            if (remaining > 0) {
                long minutes = remaining / 60000;
                long seconds = (remaining % 60000) / 1000;
                return String.format("%02d:%02d", minutes, seconds);
            }
        }
        return "00:00";
    }

    /**
     * Looks up the user's registered email from the signup table using the PIN hash.
     */
    private String lookupEmailByPinHash(String pinHash) {
        try {
            DatabaseConnection c = new DatabaseConnection();
            Connection conn = c.connection;
            String query = "SELECT email FROM signup WHERE pin = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, pinHash);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String email = rs.getString("email");
                rs.close();
                ps.close();
                return email;
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            // Do not print sensitive info
        }
        return null;
    }

    /**
     * Generates a cryptographically secure 6-digit OTP.
     */
    private String generateSecureOtp() {
        SecureRandom random = new SecureRandom();
        int otpValue = 100000 + random.nextInt(900000); // 100000–999999
        return String.valueOf(otpValue);
    }

    /**
     * Hashes the OTP using SHA-256 for secure in-memory storage.
     * The plaintext OTP is never stored — only this hash.
     */
    private String hashOtp(String otp) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(otp.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Internal record for tracking an OTP in memory.
     */
    private static class OtpRecord {
        String otpHash;       // SHA-256 hash of the plaintext OTP
        long expiryTime;      // System.currentTimeMillis() threshold
        int attemptsLeft;     // Remaining verification attempts
    }
}
