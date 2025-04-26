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

            
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
            
            BigInteger sharedKey = DHKeyExchange.clientSideSharedKeyCreation(out, in);
            this.aesKey = AES.deriveAESKey(sharedKey.toByteArray());

		} catch (Exception e) {
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
                System.out.println(username+" loggedin");
                ChatGUI.setPrimaryStageTitle(username+ " welcome to CryptoChat!");
                this.ioManager = new IOManager(socket, out, in, username);
                
                return true;
            } else if(decryptedResponse.equals("/authenticationFailed")) {
                return false;
            }
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

    public String sendMsg(String msg) {
        return ioManager.sendMsg(msg);
    }
}
