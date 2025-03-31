package chat.Server;

import chat.Shared.AES;
import chat.Shared.DHKeyExchange;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;

import javax.crypto.SecretKey;

public class Server {
	private static final int PORT=1234;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("Server listening on port " + PORT);

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) 
                {
                    System.out.println("Client connected!");
                    //ClientList.add(clientSocket);
                    String nome = clientAuthentication(clientSocket);
                    if(nome != null) {
                        ClientList.add(clientSocket, nome);
                    }
                } catch (Exception e) {
                    System.err.println("Error handling connection: " + e.getMessage());
                }
            }
        } catch(Exception e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }

    private static String clientAuthentication(Socket clientSocket) {
        try(ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream()); 
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));)
        {
            /*
            // Step 1: Perform Diffie-Hellman Key Exchange
            BigInteger p = new BigInteger("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1"
            + "29024E088A67CC74020BBEA63B139B22514A08798E3404D"
            + "DEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C2"
            + "45E485B576625E7EC6F44C42E9A63A36210000000000090F", 16);
            BigInteger g = BigInteger.valueOf(2);
            BigInteger serverPrivate = new BigInteger(256, new SecureRandom());
            BigInteger serverPublic = g.modPow(serverPrivate, p);

            out.writeObject(serverPublic);
            out.flush();

            BigInteger clientPublic = (BigInteger) in.readObject();
            BigInteger sharedSecret = clientPublic.modPow(serverPrivate, p);
            */

            BigInteger sharedSecret = DHKeyExchange.serverSideSharedKeyCreation(out, in);

            // Step 2: Derive AES Key
            SecretKey aesKey = AES.deriveAESKey(sharedSecret.toByteArray());
            System.out.println("Shared Secret Derived. Ready to communicate securely.");

            // Simulated Auth
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
        } catch (Exception e) {
            System.err.println("Error handling authentication: " + e.getMessage());
            return null;
        }
    }
}
