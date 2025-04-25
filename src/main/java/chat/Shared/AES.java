package chat.Shared;

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
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;  // Recommended IV length for AES-GCM
    private static final int GCM_TAG_LENGTH = 128; // Authentication tag length

    // Generate AES-256 Key
    public static SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(AES_KEY_SIZE, new SecureRandom());
        return keyGen.generateKey();
    }

    // Convert shared secret into AES Key
    public static SecretKey deriveAESKey(byte[] sharedSecret) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = sha.digest(sharedSecret);
        return new SecretKeySpec(key, "AES");
    }

    // Encrypt using AES-GCM
    public static String encrypt(String plaintext, SecretKey key) throws Exception {
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

        return Base64.getEncoder().encodeToString(combined);
    }

    // Decrypt using AES-GCM
    public static String decrypt(String ciphertext, SecretKey key) throws Exception {
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
