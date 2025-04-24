package chat.Client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;

import javax.crypto.SecretKey;

import chat.Shared.AES;
import chat.Shared.DHKeyExchange;

public class Client {
    private static final String ADDRESS = "localhost";
	private static final int PORT=8080;
    private static Socket socket;

    
    public static void main(String[] args) {
		try {
            socket = new Socket(ADDRESS, PORT);
            System.out.println("Connected to server!");

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            String username = authenticationWithServer(out, in);
            System.out.println(username);
            if(username != null) {
                new IOManager(socket, out, in, username);
            } else {
                System.out.println("Program closed correctly");
                return;
            }

		} catch (Exception e) {
			System.out.println("Host ID not found!");
			System.exit(1);
		}
    }

    private static String authenticationWithServer(ObjectOutputStream out, ObjectInputStream in) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            
            BigInteger sharedKey = DHKeyExchange.clientSideSharedKeyCreation(out, in);
            
            SecretKey aesKey = AES.deriveAESKey(sharedKey.toByteArray());

            while (true) {
                String encryptedResponse = (String) in.readObject();
                String decryptedResponse = AES.decrypt(encryptedResponse, aesKey);
                System.out.println("Server: " + decryptedResponse);

                if(decryptedResponse.startsWith("/authenticationCorrect")){
                    return decryptedResponse.split(";;")[1];
                } else if(decryptedResponse.equalsIgnoreCase("/authenticationFailed")) {
                    return null;
                }

                System.out.print("Client: ");
                String message = reader.readLine();
                String encryptedMessage = AES.encrypt(message, aesKey);
                out.writeObject(encryptedMessage);
                out.flush();
                if(message.equalsIgnoreCase("e")) {
                    out.close();
                    in.close();
                    return null;
                }
            }
        } catch(Exception e) {
            System.err.println(e);
            return null;
        }
    }
}
