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

public class AES {
    private static final Logger logger = LogManager.getLogger(AES.class);

    static {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;  // Recommended IV length for AES-GCM
    private static final int GCM_TAG_LENGTH = 128; // Authentication tag length

    // Generate AES-256 Key
    public static SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(AES_KEY_SIZE, new SecureRandom());
        SecretKey aesKey = keyGen.generateKey();
        logger.debug("AES key generated.");
        return aesKey;
    }

    // Convert shared secret into AES Key
    public static SecretKey deriveAESKey(byte[] sharedSecret) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = sha.digest(sharedSecret);
        SecretKeySpec derivedKey = new SecretKeySpec(key, "AES");
        logger.debug("AES key derived from shared secret.");
        return derivedKey;
    }

    // Encrypt using AES-GCM
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

        logger.info("Message encrypted");
        return Base64.getEncoder().encodeToString(combined);
    }

    // Decrypt using AES-GCM
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
        
        logger.info("Message decrypted");
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    // Convert SecretKey to Base64 String
    public static String secretKeyToString(SecretKey secretKey) {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    // Convert Base64 String to SecretKey
    public static SecretKey stringToSecretKey(String keyString) {
        byte[] decodedKey = Base64.getDecoder().decode(keyString);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }
}
