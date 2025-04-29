package chat.Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;

import javax.crypto.SecretKey;

import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.config.Configurator;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import chat.Shared.AES;
import chat.Shared.DHKeyExchange;
import chat.Shared.ManageJson;


/**
 * Class that manages the client authentication from server side
 */
public class SecureAuthenticateClient {
    private static final Logger logger = LogManager.getLogger(SecureAuthenticateClient.class);

    private static final Object syncLock = new Object(); // Lock object for synchronization
    private SecretKey aesKey;

    static {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.DEBUG);
    }

    /**
     * Does key exchange with Dieffie Hellman and authenticates client
     * 
     * @param out the client ObjectOuputStream
     * @param in the client ObjectInputStream
     * @return the username of the authenticated client or null
     */
    public String SecureAuthentication(ObjectOutputStream out, ObjectInputStream in) {
        try {
            synchronized (syncLock) { // Synchronize key exchange
                BigInteger sharedKey = DHKeyExchange.serverSideSharedKeyCreation(out, in); 
                aesKey = AES.deriveAESKey(sharedKey.toByteArray());
            }
            return authentication(out, in);
        } catch(Exception e) {
            logger.error("Error during key exchange or AES key derivation", e);
            try {
                logger.error("Client failed to authenticate, closing connection");
                out.close();
                in.close();
            } catch (IOException e1) {
                logger.error("Streams not closed correctly", e1);
            }
            return null;
        }
    }

    /**
     * Handles the authentication process by receiving and processing login or registration commands.
     *
     * @param out the client ObjectOutputStream
     * @param in the client ObjectInputStream
     * @return the authenticated username if successful, or null if authentication fails
     * @throws Exception if an error occurs during communication or encryption/decryption
     */
    private String authentication(ObjectOutputStream out, ObjectInputStream in) throws Exception {
        boolean result = false;
        while(!result) {
            String encryptedMessage = (String) in.readObject();
            String command = AES.decrypt(encryptedMessage, aesKey);

            if(command.startsWith("/login")) {
                String[] loginCommand = command.split(" ");
                String username = loginCommand[1];
                String psw = loginCommand[2];
                result = login(username, psw);

                if(ClientList.find(username) != null) { // Check if already logged in
                    logger.warn("User " + username + " already logged in");
                    result = false;
                    out.writeObject(AES.encrypt("/authenticationFailed", aesKey));
                    out.flush();
                } else {
                    if(!result) {
                        out.writeObject(AES.encrypt("/authenticationFailed", aesKey));
                        out.flush();
                        logger.warn("Failed login attempt for user " + username);
                    } else {
                        out.writeObject(AES.encrypt("/authenticationCorrect", aesKey));
                        out.flush();
                        logger.info("User " + username + " logged in");
                        return username;
                    }
                }
            } else if(command.startsWith("/register")) {
                String[] registerCommand = command.split(" ");
                String username = registerCommand[1];
                String psw = registerCommand[2];
                String hashedPsw = BCrypt.hashpw(psw, BCrypt.gensalt(12)); // 12 hash factor, secure and fast
                result = register(username, hashedPsw);

                if(!result) {
                    out.writeObject(AES.encrypt("/authenticationFailed", aesKey));
                    out.flush();
                    logger.warn("User " + username + " already exists, new user not registered");
                } else {
                    out.writeObject(AES.encrypt("/authenticationCorrect", aesKey));
                    out.flush();
                    logger.info("User " + username + " registered successfully");
                    return username;
                }
            }
        }
        return null;
    }

    /**
     * Manages user registration
     * 
     * @param username client's username
     * @param hashedPassword password hashed to save
     * @return true if correctly registered or false if username already exists or error occurred
     */
    private static boolean register(String username, String hashedPassword) {
        try {
            JSONObject users = ManageJson.loadUsersFromFile();
    
            if (users.has(username)) {
                return false; // username already registered
            }
            
            // Creates an user JSONObject
            JSONObject userObj = new JSONObject();
            userObj.put("hash", hashedPassword);
    
            // Saves new users list in the file
            users.put(username, userObj);
            ManageJson.saveUsersToFile(users);
            return true;
    
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Manages user login
     * 
     * @param username client's username
     * @param password password to check
     * @return true if correctly logged in or false if username not found, if wrong password or error occurred
     */
    private static boolean login(String username, String password) {
        try {
            JSONObject users = ManageJson.loadUsersFromFile();
    
            if (!users.has(username)) {
                return false; // username not found
            }
    
            JSONObject userObj = users.getJSONObject(username);
            String hashSalvato = userObj.getString("hash");
    
            return BCrypt.checkpw(password, hashSalvato);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
