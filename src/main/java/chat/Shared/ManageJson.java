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
import org.json.JSONObject;

import chat.Client.RSAUtils;

public class ManageJson {
    private static final Logger logger = LogManager.getLogger(ManageJson.class);
    static {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);
    }


    private static final String USERS_FILE = "users.json";

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
            e.printStackTrace();
            return new JSONObject();
        }
    }

    public synchronized static void saveUsersToFile(JSONObject users) {
        try (FileWriter file = new FileWriter(USERS_FILE)) {
            file.write(users.toString(4)); // formatted
            logger.info("Users saved to file.");
        } catch (Exception e) {
            logger.error("Error saving users: " + e.getMessage(), e);
        }
    }

    public synchronized static boolean addOrEditPublicKey(String username, String publicKey) {
        try {
            JSONObject users = loadUsersFromFile();

            if (!users.has(username)) {
                logger.error("User not found, key not loaded: " + username);
                return false;
            }

            JSONObject userObj = users.getJSONObject(username);
            userObj.put("publicKey", publicKey); // Add or update publicKey
            users.put(username, userObj); // Not strictly needed, but safe

            saveUsersToFile(users);
            logger.info("Public key added for user: " + username);
            return true;

        } catch (Exception e) {
            logger.error("Error adding public key for " + username + ": " + e.getMessage(), e);
            return false;
        }
    }

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
                            // Convert the publicKey string to a PublicKey object and add it to the map
                            PublicKey pubKey = RSAUtils.stringToPublicKey(publicKey);
                            usersWithKey.put(username, pubKey);
                        } catch (Exception e) {
                            logger.error("Error converting public key for " + username + ": " + e.getMessage(), e);
                        }
                    }
                }
            }
        }
        
        logger.info("Users with public keys retrieved.");
        return usersWithKey;
    }
    
}
