package chat.Client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import chat.Shared.AES;
import chat.Shared.PacketManager;

/**
 * Manages IO streams of the client
 */
public class IOManager {
    private static final Logger logger = LogManager.getLogger(IOManager.class);
    static {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);
    }

    private Socket socket;
    private String username;
    private OutputManager outputManager;
    private InputManager inputManager;
    private KeyPair keys;

    /**
     * Constructor of the class.
     * 
     * Saves client's data and streams, creates the RSA KeyPair and populate the OnlineList from the JSON. Adds the ioManager to the ServerCommandManager.
     * 
     * @param clientSocket client's socket
     * @param out client's ObjectOutputStrem
     * @param in client's ObjectInputStrem
     * @param username client's username
     */
    public IOManager(Socket clientSocket, ObjectOutputStream out, ObjectInputStream in, String username) {
        try {
            this.username = username;
            this.socket = clientSocket;
            this.keys = RSAUtils.generateKeyPair();
            OnlineList.loadFromJSON(username);
            ServerCommandManager.addIOManager(this);
            
            this.outputManager = new OutputManager(out);
            this.inputManager = new InputManager(in, this);

            logger.info("IOManager initialized for user: {}", username);

        } catch (Exception e) {
            logger.error("Error initializing IOManager for user {}: {}", username, e.getMessage());
        }
    }


    /**
     * Getter for the RSA public key
     * 
     * @return the RSA public key
     */
    public PublicKey getPublicKey() {
        return keys.getPublic();
    }

    /**
     * Getter for the RSA private key
     * 
     * @return the RSA private key
     */
    public PrivateKey getPrivateKey() {
        return keys.getPrivate();
    }

    /**
     * Manages messages, performs Packet creation for messages to other clients.
     * 
     * @param msg the message
     * @return null if message sent correctly, error message for the GUI otherwise
     */
    public String sendMsg(String msg) {
        if(!msg.startsWith("/")) {
            try {
                SecretKey aesKey = AES.generateAESKey();
                String stringAesKey = AES.secretKeyToString(aesKey);
                String encryptedMsg = AES.encrypt(msg, aesKey);
                String encryptedAesKey = RSAUtils.encryptWithPrivateKey(stringAesKey, this.getPrivateKey());
                String packet = PacketManager.createMsgPacket(this.username, "all", encryptedMsg, encryptedAesKey);
                
                logger.info("Sending message to all: {}", msg);

                outputManager.sendMsg(packet);
                return null;
            } catch (Exception e) {
                logger.error("Error sending message to all: {}", e.getMessage());
                return "Error! Message not sent";
            }
        } else {
            if(msg.startsWith("/DM")) {
                try {
                    String[] command = msg.split(";;");
                    String dest = command[1];
                    String msgString = command[2];

                    SecretKey aesKey = AES.generateAESKey();
                    String stringAesKey = AES.secretKeyToString(aesKey);
                    String encryptedMsg = AES.encrypt(msgString, aesKey);

                    if(OnlineList.getClientKey(dest) != null) {
                        // Encryption with client public key and sign with self private key
                        String firstEncryptedAesKey = RSAUtils.encryptWithPublicKey(stringAesKey, OnlineList.getClientKey(dest));

                        String secondEncryptedAesKey = RSAUtils.encryptWithPrivateKey(firstEncryptedAesKey, this.getPrivateKey());

                        String packet = PacketManager.createMsgPacket(this.username, dest, encryptedMsg, secondEncryptedAesKey);

                        logger.info("Sending direct message to {}: {}", dest, msgString);

                        outputManager.sendMsg(packet);
                        return null;
                    } else {
                        logger.warn("Client {} not found for direct message", dest);
                        return "Client not found! Message not sent";
                    }
                } catch(Exception e) {
                    logger.error("Error processing direct message: {}", e.getMessage());
                    return "Message format error, message not sent!";
                }
            } else if(msg.startsWith("/help")) {
                outputManager.sendMsg(msg);
                return null;
            }
        }
        return "Invalid command!";
    }

    /**
     * Sends message directly to server without checking for format.
     * 
     * @param msg message to send
     */
    public void sendToServer(String msg) {
        logger.info("Sending message to server: {}", msg);
        outputManager.sendMsg(msg);
    }
}
