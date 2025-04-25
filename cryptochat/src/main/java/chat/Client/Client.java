package chat.Client;

import java.io.IOException;
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
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private SecretKey aesKey;
    private String username;
    private IOManager ioManager;
    
    public void connectWithServer() {
		try {
            socket = new Socket(ADDRESS, PORT);
            System.out.println("Connected to server!");

            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());

            BigInteger sharedKey = DHKeyExchange.clientSideSharedKeyCreation(out, in);
            this.aesKey = AES.deriveAESKey(sharedKey.toByteArray());
		} catch (Exception e) {
			System.out.println("Host ID not found!");
			System.exit(1);
		}
    }

    public boolean loginOrRegister(String action, String username, String password) {
        try {
            String loginMsg = "/"+action+" "+username+" "+password;
            String encryptedMsg = AES.encrypt(loginMsg, aesKey);
            out.writeObject(encryptedMsg);
            out.flush();

            String response = (String) in.readObject();
            String decryptedResponse = AES.decrypt(response, aesKey);

            if(decryptedResponse.equals("/authenticationCorrect")){
                this.username = username;

                this.ioManager = new IOManager(socket, out, in, username);
                
                return true;
            } else if(decryptedResponse.equals("/authenticationFailed")) {
                return false;
            }
            /*while (true) {
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
            }*/
        } catch(Exception e) {
            System.err.println(e);
            return false;
        }
        return false;
    }

    public void disconnect() {
        try {
            socket.close();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        ioManager.sendMsg(msg);
    }
}
