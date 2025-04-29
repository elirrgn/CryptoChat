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

/**
 * Utility class providing RSA encryption and decryption methods.
 * This class uses Bouncy Castle as the cryptographic provider.
 */
public class RSAUtils {

    private static final Logger logger = LogManager.getLogger(RSAUtils.class);

    static {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.DEBUG);
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final int RSA_KEY_SIZE = 2048;
    private static final int RSA_MAX_ENCRYPT_BLOCK = 245;
    private static final int RSA_MAX_DECRYPT_BLOCK = 256;

    /**
     * Generates a new RSA key pair.
     * 
     * @return A KeyPair containing a public and private key.
     * @throws Exception If an error occurs during key generation.
     */
    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
        keyGen.initialize(RSA_KEY_SIZE);
        KeyPair keyPair = keyGen.generateKeyPair();
        logger.debug("RSA key pair generated successfully.");
        return keyPair;
    }

    /**
     * Encrypts the provided plaintext with the given public key.
     * 
     * @param plainText The data to encrypt.
     * @param publicKey The public key used for encryption.
     * @return The encrypted data as a Base64 encoded string.
     * @throws Exception If an error occurs during encryption.
     */
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
        logger.debug("Data encrypted successfully.");

        return Base64.getEncoder().encodeToString(finalEncrypted);
    }

    /**
     * Encrypts the provided data with the given private key (signing).
     * 
     * @param data The data to sign.
     * @param privateKey The private key used for encryption.
     * @return The signed data as a Base64 encoded string.
     * @throws Exception If an error occurs during encryption.
     */
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

        logger.debug("Data encrypted successfully.");
        return Base64.getEncoder().encodeToString(finalEncrypted);
    }

    /**
     * Decrypts the provided Base64 encoded data using the given public key.
     * 
     * @param encryptedBase64 The encrypted data as a Base64 encoded string.
     * @param publicKey The public key used for decryption.
     * @return The decrypted plaintext.
     * @throws Exception If an error occurs during decryption.
     */
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

        logger.debug("Data decrypted successfully.");
        return new String(finalDecrypted, StandardCharsets.UTF_8);
    }

    /**
     * Decrypts the provided Base64 encoded data using the given private key.
     * 
     * @param encryptedBase64 The encrypted data as a Base64 encoded string.
     * @param privateKey The private key used for decryption.
     * @return The decrypted plaintext.
     * @throws Exception If an error occurs during decryption.
     */
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
        
        logger.debug("Data decrypted successfully.");
        return new String(finalDecrypted, StandardCharsets.UTF_8);
    }

    /**
     * Converts a public key to a Base64 encoded string.
     * 
     * @param publicKey The public key to convert.
     * @return The public key as a Base64 encoded string.
     */
    public static String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * Converts a Base64 encoded string to a public key.
     * 
     * @param keyStr The Base64 encoded string representing the public key.
     * @return The corresponding public key.
     * @throws Exception If an error occurs during the conversion.
     */
    public static PublicKey stringToPublicKey(String keyStr) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(keyStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * Converts a private key to a Base64 encoded string.
     * 
     * @param privateKey The private key to convert.
     * @return The private key as a Base64 encoded string.
     */
    public static String privateKeyToString(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    /**
     * Converts a Base64 encoded string to a private key.
     * 
     * @param keyStr The Base64 encoded string representing the private key.
     * @return The corresponding private key.
     * @throws Exception If an error occurs during the conversion.
     */
    public static PrivateKey stringToPrivateKey(String keyStr) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(keyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * Joins a list of byte arrays into a single byte array.
     * 
     * @param chunks The list of byte arrays to join.
     * @return The combined byte array.
     */
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
