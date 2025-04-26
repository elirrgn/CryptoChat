package chat.Client;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class RSAUtils {

    private static final Logger logger = LogManager.getLogger(RSAUtils.class);
    static {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final int RSA_KEY_SIZE = 2048;
    private static final int RSA_MAX_ENCRYPT_BLOCK = 245; // 2048-bit key with PKCS1Padding = 245 bytes max plaintext
    private static final int RSA_MAX_DECRYPT_BLOCK = 256; // 2048-bit key = 256 bytes ciphertext block size

    // Generate RSA key pair
    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
        keyGen.initialize(RSA_KEY_SIZE);
        KeyPair keyPair = keyGen.generateKeyPair();
        logger.info("RSA key pair generated successfully.");
        return keyPair;
    }

    // Encrypt with the foreign public key
    public static String encryptWithPublicKey(String plainText, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] data = plainText.getBytes(StandardCharsets.UTF_8);
        List<byte[]> encryptedChunks = new ArrayList<>();
        int offset = 0;

        while (data.length - offset > 0) {
            int blockSize = Math.min(data.length - offset, RSA_MAX_ENCRYPT_BLOCK);
            encryptedChunks.add(cipher.doFinal(data, offset, blockSize));
            offset += blockSize;
        }

        byte[] finalEncrypted = joinChunks(encryptedChunks);
        logger.info("Data encrypted successfully.");

        return Base64.getEncoder().encodeToString(finalEncrypted);
    }

    // Encrypt with your private key (signing the data)
    public static String encryptWithPrivateKey(String data, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);

        byte[] rawData = data.getBytes(StandardCharsets.UTF_8);
        List<byte[]> encryptedChunks = new ArrayList<>();
        int offset = 0;

        while (rawData.length - offset > 0) {
            int blockSize = Math.min(rawData.length - offset, RSA_MAX_ENCRYPT_BLOCK);
            encryptedChunks.add(cipher.doFinal(rawData, offset, blockSize));
            offset += blockSize;
        }

        byte[] finalEncrypted = joinChunks(encryptedChunks);

        logger.info("Data encrypted successfully.");
        return Base64.getEncoder().encodeToString(finalEncrypted);
    }

    // Decrypt with your public key (first step of decryption)
    public static String decryptWithPublicKey(String encryptedBase64, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);

        byte[] encryptedData = Base64.getDecoder().decode(encryptedBase64);
        List<byte[]> decryptedChunks = new ArrayList<>();
        int offset = 0;

        while (encryptedData.length - offset > 0) {
            int blockSize = Math.min(encryptedData.length - offset, RSA_MAX_DECRYPT_BLOCK);
            decryptedChunks.add(cipher.doFinal(encryptedData, offset, blockSize));
            offset += blockSize;
        }

        byte[] finalDecrypted = joinChunks(decryptedChunks);

        logger.info("Data decrypted successfully.");
        return new String(finalDecrypted, StandardCharsets.UTF_8);
    }

    // Decrypt with the foreign private key (second step of decryption)
    public static String decryptWithPrivateKey(String encryptedBase64, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] encryptedData = Base64.getDecoder().decode(encryptedBase64);
        List<byte[]> decryptedChunks = new ArrayList<>();
        int offset = 0;

        while (encryptedData.length - offset > 0) {
            int blockSize = Math.min(encryptedData.length - offset, RSA_MAX_DECRYPT_BLOCK);
            decryptedChunks.add(cipher.doFinal(encryptedData, offset, blockSize));
            offset += blockSize;
        }

        byte[] finalDecrypted = joinChunks(decryptedChunks);
        
        logger.info("Data decrypted successfully.");
        return new String(finalDecrypted, StandardCharsets.UTF_8);
    }

    // Convert public key to a string
    public static String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    // Convert string to public key
    public static PublicKey stringToPublicKey(String keyStr) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(keyStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    // Convert private key to string
    public static String privateKeyToString(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    // Convert string to private key
    public static PrivateKey stringToPrivateKey(String keyStr) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(keyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    // Helper to join encrypted/decrypted chunks
    private static byte[] joinChunks(List<byte[]> chunks) {
        int totalLength = 0;
        for (byte[] chunk : chunks) {
            totalLength += chunk.length;
        }
        byte[] result = new byte[totalLength];
        int pos = 0;
        for (byte[] chunk : chunks) {
            System.arraycopy(chunk, 0, result, pos, chunk.length);
            pos += chunk.length;
        }
        return result;
    }
}
