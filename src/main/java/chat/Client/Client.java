package chat.Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;

import javax.crypto.SecretKey;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import chat.Shared.AES;
import chat.Shared.DHKeyExchange;

/**
 * Main Client class for connecting, disconnecting and sending message to the server
 */
public class Client {
    private static final Logger logger = LogManager.getLogger(Client.class);
    static {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);
    }

    private static final String ADDRESS = "localhost";
	private static final int PORT=8080;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private SecretKey aesKey;
    private String username;
    private IOManager ioManager;
    
    /**
     * Method that connects to the Server and performs Dieffie Hellman key exchange
     */
    public void connectWithServer() {
		try {
            socket = new Socket(ADDRESS, PORT);
            logger.info("Attempting to connect to server at {}:{}", ADDRESS, PORT);
            
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
            logger.info("Connection established. Performing DH Key Exchange...");
            
            BigInteger sharedKey = DHKeyExchange.clientSideSharedKeyCreation(out, in);
            this.aesKey = AES.deriveAESKey(sharedKey.toByteArray());
            logger.info("Key exchange successful. AES Key derived.");
		} catch (Exception e) {
            logger.error("Error while connecting to server or performing key exchange", e);
			System.exit(1);
		}
    }

    /**
     * Method to do the encrypted authentication with the server, sends commands to the server and authenticates.
     * 
     * @param action action to take: "login" or "register"
     * @param username the username for the authentication
     * @param password the password for the authentication
     * @return true if authenticated correctly, false if authentication failed or error
     */
    public boolean loginOrRegister(String action, String username, String password) {
        try {
            logger.info("Attempting {} with username: {}", action, username);
            String loginMsg = "/"+action+" "+username+" "+password;
            String encryptedMsg = AES.encrypt(loginMsg, aesKey);
            out.writeObject(encryptedMsg);
            out.flush();

            String response = (String) in.readObject();
            String decryptedResponse = AES.decrypt(response, aesKey);

            if(decryptedResponse.equals("/authenticationCorrect")){
                this.username = username;
                logger.info("{} logged in successfully", username);
                ChatGUI.setPrimaryStageTitle(username+ " welcome to CryptoChat!");
                this.ioManager = new IOManager(socket, out, in, username);
                return true;
            } else if(decryptedResponse.equals("/authenticationFailed")) {
                logger.warn("Authentication failed for username: {}", username);
                return false;
            }
        } catch(Exception e) {
            logger.error("Error during authentication for username: {}", username, e);
            return false;
        }
        return false;
    }

    /**
     * Method to disconnect from the server.
     */
    public void disconnect() {
        try {
            logger.info("Disconnecting from server...");
            socket.close();
            System.exit(0);
        } catch (IOException e) {
            logger.error("Error while disconnecting from server", e);
        }
    }

    /**
     * Send message to ioManager that will check the format
     * 
     * @param msg the message passed
     * @return ioManager.sendMsg return
     */
    public String sendMsg(String msg) {
        return ioManager.sendMsg(msg);
    }
}
