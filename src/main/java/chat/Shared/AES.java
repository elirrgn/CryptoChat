package chat.Shared;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

/**
 * Utility class for AES-256 encryption, decryption, and key management.
 */
public class AES {
    private static final Logger logger = LogManager.getLogger(AES.class);

    static {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.DEBUG);
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;  // Recommended IV length for AES-GCM
    private static final int GCM_TAG_LENGTH = 128; // Authentication tag length

    /**
     * Generates a new AES-256 secret key.
     *
     * @return the generated AES key
     * @throws NoSuchAlgorithmException if the AES algorithm is not available
     */
    public static SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(AES_KEY_SIZE, new SecureRandom());
        SecretKey aesKey = keyGen.generateKey();
        logger.debug("AES key generated.");
        return aesKey;
    }

    /**
     * Derives an AES key from a shared secret.
     *
     * @param sharedSecret the shared secret bytes
     * @return the derived AES key
     * @throws NoSuchAlgorithmException if the SHA-256 algorithm is not available
     */    
    public static SecretKey deriveAESKey(byte[] sharedSecret) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = sha.digest(sharedSecret);
        SecretKeySpec derivedKey = new SecretKeySpec(key, "AES");
        logger.debug("AES key derived from shared secret.");
        return derivedKey;
    }

    /**
     * Encrypts a plaintext string using AES-GCM.
     *
     * @param plaintext the data to encrypt
     * @param key the AES key used for encryption
     * @return the encrypted data encoded as a Base64 string
     * @throws Exception if an encryption error occurs
     */
    public static String encrypt(String plaintext, SecretKey key) throws Exception {
        logger.debug("Encrypting data using AES-GCM.");

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");

        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv); // Generate a random IV

        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

        byte[] encryptedData = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        // Combine IV + Encrypted Data
        byte[] combined = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);

        logger.debug("Message encrypted");
        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * Decrypts an AES-GCM encrypted string.
     *
     * @param ciphertext the Base64 encoded encrypted data
     * @param key the AES key used for decryption
     * @return the decrypted plaintext
     * @throws Exception if a decryption error occurs
     */
    public static String decrypt(String ciphertext, SecretKey key) throws Exception {
        logger.debug("Decrypting data using AES-GCM.");

        byte[] decoded = Base64.getDecoder().decode(ciphertext);

        // Extract IV and Ciphertext
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] encryptedData = new byte[decoded.length - GCM_IV_LENGTH];

        System.arraycopy(decoded, 0, iv, 0, iv.length);
        System.arraycopy(decoded, iv.length, encryptedData, 0, encryptedData.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

        byte[] decryptedData = cipher.doFinal(encryptedData);
        
        logger.debug("Message decrypted");
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    /**
     * Converts a secret key to a Base64 encoded string.
     *
     * @param secretKey the secret key to convert
     * @return the Base64 encoded string
     */
    public static String secretKeyToString(SecretKey secretKey) {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    /**
     * Converts a Base64 encoded string back to a secret key.
     *
     * @param keyString the Base64 encoded key string
     * @return the reconstructed secret key
     */
    public static SecretKey stringToSecretKey(String keyString) {
        byte[] decodedKey = Base64.getDecoder().decode(keyString);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }
}
