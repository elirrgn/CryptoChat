package chat.Client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.SecureRandom;

import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHKey;

import chat.Shared.AES;
import chat.Shared.DHKeyExchange;

public class Client {
    private static final String ADDRESS = "localhost";
	private static final int PORT=1234;
    private static Socket socket;

    public static void main(String[] args) {
		try {
            socket = new Socket(ADDRESS, PORT);
            System.out.println("Connected to server!");

            if(authenticationWithServer(socket)) {
                System.out.println("Client Authenticated");
            } else {
                return;
            }

		} catch (Exception e) {
			System.out.println("Host ID not found!");
			System.exit(1);
		}
    }

    private static boolean authenticationWithServer(Socket socket) {
        try (
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        ) {
            /*
            // Step 1: Perform Diffie-Hellman Key Exchange
            BigInteger p = new BigInteger("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1"
                    + "29024E088A67CC74020BBEA63B139B22514A08798E3404D"
                    + "DEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C2"
                    + "45E485B576625E7EC6F44C42E9A63A36210000000000090F", 16);
            BigInteger g = BigInteger.valueOf(2);
            BigInteger clientPrivate = new BigInteger(256, new SecureRandom());
            BigInteger clientPublic = g.modPow(clientPrivate, p);

            BigInteger serverPublic = (BigInteger) in.readObject();
            out.writeObject(clientPublic);
            out.flush();

            BigInteger sharedSecret = serverPublic.modPow(clientPrivate, p);

            // Step 2: Derive AES Key
            SecretKey aesKey = AES.deriveAESKey(sharedSecret.toByteArray());
            System.out.println("Shared Secret Derived. Ready to communicate securely.");
            */

            BigInteger sharedKey = DHKeyExchange.clientSideSharedKeyCreation(out, in);
            
            SecretKey aesKey = AES.deriveAESKey(sharedKey.toByteArray());

            // Step 3: Secure Chat
            while (true) {
                String encryptedResponse = (String) in.readObject();
                String decryptedResponse = AES.decrypt(encryptedResponse, aesKey);
                System.out.println("Server: " + decryptedResponse);

                if(decryptedResponse.equalsIgnoreCase("/authenticatedCorrectly")){
                    return true;
                } else if(decryptedResponse.equalsIgnoreCase("/authenticationError")) {
                    return false;
                }

                System.out.print("Client: ");
                String message = reader.readLine();
                String encryptedMessage = AES.encrypt(message, aesKey);
                out.writeObject(encryptedMessage);
                out.flush();
            }
        } catch(Exception e) {
            System.err.println(e);
            return false;
        }
    }
}
