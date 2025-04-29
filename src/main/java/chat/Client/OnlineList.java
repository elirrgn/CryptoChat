package chat.Client;

import java.security.PublicKey;
import java.util.HashMap;

import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.config.Configurator;

import chat.Shared.ManageJson;

/**
 * Manages the online client list and synchronizes with the GUI 
 */
public class OnlineList {
    private static final Logger logger = LogManager.getLogger(OnlineList.class);

    private static HashMap<String, PublicKey> clientKeys = new HashMap<String, PublicKey>(); // ClientName, Client public Key
    
    static {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.DEBUG);
    }

    /**
     * Loads the Online clients and their PublicKeys from the JSON, updates the GUI
     * 
     * @param username personal username, to avoid
     */
    public static void loadFromJSON(String username) {
        clientKeys = ManageJson.getUsersWithPublicKey(username);
        ChatGUI.loadOnlineClients(clientKeys.keySet());
        logger.debug("Client Online List Added");
    }

    /**
     * Adds a client and its publickey to the HashMap, updates the GUI
     * 
     * @param clientName username of the client
     * @param publicKey client's publickey
     */
    public static void addClient(String clientName, PublicKey publicKey) {
        clientKeys.put(clientName, publicKey);
        ChatGUI.addOnlineUser(clientName);
        logger.debug("Added client: "+clientName);;
    }
    
    /**
     * Removes a client and its publickey to the HashMap, updates the GUI
     * 
     * @param clientName username of the client
     */
    public static void removeClient(String clientName) {
        clientKeys.remove(clientName);
        ChatGUI.removeOnlineUser(clientName);
        logger.debug("Removed client: "+clientName);;
    }
    
    /**
     * Getter for a client key.
     * 
     * @param clientName client to get the publickey
     * @return the correct publickey or null
     */
    public static PublicKey getClientKey(String clientName){
        return clientKeys.get(clientName);
    }
}
