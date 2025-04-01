package chat.Server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;

import chat.Shared.AES;
import chat.Shared.DHKeyExchange;

public class SecureAuthenticateClient {
    private static BigInteger sharedKey;
    private static SecretKey aesKey;

    public static String SecureAuthentication(ObjectOutputStream out, ObjectInputStream in) {
        try {
            keyExchange(out, in);
            deriveAESKey();
            return authentication(out, in);
        } catch(Exception e) {
            System.out.println(e.getLocalizedMessage());
            return null;
        }
    }

    private static void keyExchange(ObjectOutputStream out, ObjectInputStream in) throws Exception {
        sharedKey = DHKeyExchange.serverSideSharedKeyCreation(out, in);
    }

    private static void deriveAESKey() throws NoSuchAlgorithmException {
        aesKey = AES.deriveAESKey(sharedKey.toByteArray());
    }

    private static String authentication(ObjectOutputStream out, ObjectInputStream in) throws Exception {
        String message = "Send username please!";
        String encryptedResponse = AES.encrypt(message, aesKey);
        out.writeObject(encryptedResponse);
        out.flush();

        String encryptedMessage = (String) in.readObject();
        String decryptedUsername = AES.decrypt(encryptedMessage, aesKey);
                
        message = "Send password please!";
        encryptedResponse = AES.encrypt(message, aesKey);
        out.writeObject(encryptedResponse);
        out.flush();

        encryptedMessage = (String) in.readObject();
        String decryptedPsw = AES.decrypt(encryptedMessage, aesKey);

        if(decryptedUsername.equals("elirrgn") && decryptedPsw.equals("Argelir")) {
            out.writeObject(AES.encrypt("/authenticatedCorrectly", aesKey));
            return decryptedUsername;
        } else {
            out.writeObject(AES.encrypt("/authenticationError", aesKey));
            return null;
        }
    }
    
}
