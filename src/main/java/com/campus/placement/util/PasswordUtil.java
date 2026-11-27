package com.campus.placement.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Objects;

/**
 * Salted password hashing and verification utility using PBKDF2WithHmacSHA256.
 *
 * <p><b>Why PBKDF2?</b> We use {@code PBKDF2WithHmacSHA256} via standard
 * {@code javax.crypto} instead of a third-party library so the project stays
 * dependency-minimal while providing strong, salted, key-stretched password
 * storage (65,536 iterations, 16-byte random salt, 256-bit derived key) and
 * constant-time verification via {@link MessageDigest#isEqual(byte[], byte[])}.</p>
 */
// [Unit 1: static methods] & [Unit 2: Objects]
public final class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65_536;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_BYTES = 16;

    private PasswordUtil() {
    }

    /**
     * Hashes a plain-text password with a freshly generated cryptographic salt.
     *
     * @param plainPassword non-null plain-text password
     * @return formatted hash string {@code iterations:base64Salt:base64Hash}
     */
    public static String hashPassword(String plainPassword) {
        Objects.requireNonNull(plainPassword, "Password must not be null");
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] hash = pbkdf2(plainPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);
        return ITERATIONS
                + ":" + Base64.getEncoder().encodeToString(salt)
                + ":" + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Verifies a plain-text password against a stored PBKDF2 hash string.
     *
     * @param plainPassword candidate plain-text password
     * @param storedHash    stored hash in {@code iterations:base64Salt:base64Hash} format
     * @return true if the password matches
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null || storedHash.isBlank()) {
            return false;
        }
        String[] parts = storedHash.split(":");
        if (parts.length != 3) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[2]);
            byte[] candidateHash = pbkdf2(plainPassword.toCharArray(), salt, iterations, expectedHash.length * 8);
            return MessageDigest.isEqual(expectedHash, candidateHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLengthBits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLengthBits);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 hashing unavailable", e);
        }
    }
}
