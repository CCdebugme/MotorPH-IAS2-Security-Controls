package com.group33.cp2.motorph.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES-128 encrypt/decrypt utility.
 *
 * Not used by the active BCrypt login flow;
 * retained for any legacy encrypted data fields.
 *
 * Security note: The secret key is loaded from the system environment variable
 * MOTORPH_SECRET_KEY at runtime. It is no longer hardcoded in source code to
 * prevent accidental exposure via version control or repository access.
 *
 * To set the key before running the application:
 *   Windows:  set MOTORPH_SECRET_KEY=YourSixteenCharK
 *   Linux/Mac: export MOTORPH_SECRET_KEY=YourSixteenCharK
 *
 * The key must be exactly 16 characters for AES-128.
 * If the environment variable is not set, a fallback default is used for
 * development only — this must be replaced before production deployment.
 */
public class CryptoUtil {

    private static final Logger log = LoggerFactory.getLogger(CryptoUtil.class);

    /**
     * AES-128 secret key loaded from environment variable MOTORPH_SECRET_KEY.
     * Falls back to a development default if the variable is not set.
     * Must be exactly 16 characters for AES-128 compatibility.
     */
    private static final String SECRET_KEY = loadSecretKey();

    private static final String ALGORITHM = "AES";

    /**
     * Loads the secret key from the MOTORPH_SECRET_KEY environment variable.
     * Logs a warning if the variable is not set and the fallback default is used.
     *
     * @return the secret key string (16 characters)
     */
    private static String loadSecretKey() {
        String key = System.getenv("MOTORPH_SECRET_KEY");
        if (key == null || key.isBlank()) {
            log.warn("CryptoUtil: MOTORPH_SECRET_KEY environment variable not set. "
                   + "Using development fallback — replace before production deployment.");
            key = "MotorPHDevKey123"; // development fallback only — 16 chars AES-128
        }
        if (key.length() != 16) {
            log.error("CryptoUtil: secret key must be exactly 16 characters for AES-128. "
                    + "Current key length: {}", key.length());
        }
        return key;
    }

    /**
     * Encrypts a plain-text string using AES-128.
     * Returns a Base64-encoded ciphertext string, or null if encryption fails.
     *
     * @param strToEncrypt the plain-text string to encrypt
     * @return Base64-encoded encrypted string, or null on failure
     */
    public static String encrypt(String strToEncrypt) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(
                    SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(
                    strToEncrypt.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("CryptoUtil.encrypt failed: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Decrypts a Base64-encoded AES-128 ciphertext string.
     * Returns the original plain-text string, or null if decryption fails.
     *
     * @param strToDecrypt the Base64-encoded encrypted string to decrypt
     * @return decrypted plain-text string, or null on failure
     */
    public static String decrypt(String strToDecrypt) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(
                    SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes = Base64.getDecoder().decode(strToDecrypt);
            byte[] decrypted    = cipher.doFinal(decodedBytes);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("CryptoUtil.decrypt failed: {}", e.getMessage(), e);
        }
        return null;
    }
}
