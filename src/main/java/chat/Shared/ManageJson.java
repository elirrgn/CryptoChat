package chat.Shared;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.util.HashMap;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.json.JSONException;
import org.json.JSONObject;

import chat.Client.RSAUtils;


/**
 * Utility class to manage the JSON file interactions
 */
public class ManageJson {
    private static final Logger logger = LogManager.getLogger(ManageJson.class);
    static {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);
    }

    private static final String USERS_FILE = "users.json";

    /**
     * Gets the JSONObject corrispondent to the file content
     * 
     * @return the correct JSONObject or an empty one if file not found or error
     */
    public synchronized static JSONObject loadUsersFromFile() {
        try {
            if (!Files.exists(Paths.get(USERS_FILE))) {
                return new JSONObject();
            }

            String content = new String(Files.readAllBytes(Paths.get(USERS_FILE)));
            logger.info("Users loaded from file");
            return new JSONObject(content);
        } catch (Exception e) {
            logger.error("Error loading users: " + e.getMessage(), e);
            return new JSONObject();
        }
    }


    /**
     * Saves the JSONObject into the file
     * 
     * @param users the JSONObject to save
     */
    public synchronized static void saveUsersToFile(JSONObject users) {
        try (FileWriter file = new FileWriter(USERS_FILE)) {
            file.write(users.toString(4)); // formatted
            logger.info("Users saved to file.");
        } catch (Exception e) {
            logger.error("Error saving users: " + e.getMessage(), e);
        }
    }

    /**
     * Reads the JSON from file and edit or add the publickey
     * 
     * @param username to whom the key needs to be edited
     * @param publicKey the new public key
     * @return true if public key added correctly, false if username not found or Exception
     */
    public synchronized static boolean addOrEditPublicKey(String username, String publicKey) {
        JSONObject users = loadUsersFromFile();
        
        if (!users.has(username)) {
            logger.error("User not found, key not loaded: " + username);
            return false;
        }

        JSONObject userObj;
        try {
            userObj = users.getJSONObject(username);
            userObj.put("publicKey", publicKey); // Add or update publicKey
            users.put(username, userObj); // Not strictly needed, but safe
        } catch (JSONException e) {
            logger.error("Error adding public key for " + username + ": " + e.getMessage(), e);
            return false;
        }

        saveUsersToFile(users);
        logger.info("Public key added for user: " + username);
        return true;

    }

    /**
     * Gets a map containing all the users currently with a valued publicKey field
     * 
     * @param clientName who makes the request, the one to exclude
     * @return an HashMap<String, PublicKey> containing the username->publicKey pairs or an empty one
     */
    public synchronized static HashMap<String, PublicKey> getUsersWithPublicKey(String clientName) {
        HashMap<String, PublicKey> usersWithKey = new HashMap<>();
        JSONObject users = loadUsersFromFile();
    
        for (String username : users.keySet()) {
            if (!username.equals(clientName)) { // Exclude the requesting client
                JSONObject userInfo = users.optJSONObject(username); // Get user info
                if (userInfo != null && userInfo.has("publicKey")) {
                    // Check if the user has a publicKey
                    String publicKey = userInfo.optString("publicKey", null);
                    if (publicKey != null && !publicKey.isEmpty()) {
                        try {
                            // Convert the publicKey string to a PublicKey object and adds it to the map
                            PublicKey pubKey = RSAUtils.stringToPublicKey(publicKey);
                            usersWithKey.put(username, pubKey);

                        } catch (Exception e) {
                            logger.error("Error converting public key for " + username + ": " + e.getMessage(), e);
                            return new HashMap<>();
                        }
                    }
                }
            }
        }
        
        logger.info("Users with public keys retrieved.");
        return usersWithKey;
    }
    
    /**
     * Iterates through the JSON file and sets the public key to null for users
     * who currently have a valued public key.
     */
    public synchronized static void deleteOnlinePublicKeys() {
        JSONObject users = loadUsersFromFile();

        // Iterate through all the users in the JSON file
        for (String username : users.keySet()) {
            JSONObject userObj = users.optJSONObject(username); // Get user info
            
            if (userObj != null && userObj.has("publicKey")) {
                // Set the publicKey to null if it's present
                userObj.remove("publicKey");
                users.put(username, userObj); // Update the user object
                logger.info("Public key set to null for user: " + username);
            }
        }

        // Save the updated user data back to the file
        saveUsersToFile(users);
        logger.info("Public keys set to null for all users with a public key.");
    }


}
