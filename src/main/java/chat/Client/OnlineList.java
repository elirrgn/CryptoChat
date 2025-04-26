package chat.Client;

import java.security.PublicKey;
import java.util.HashMap;

import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.config.Configurator;

import chat.Shared.ManageJson;

public class OnlineList {
    private static final Logger logger = LogManager.getLogger(OnlineList.class);

    private static HashMap<String, PublicKey> clientKeys = new HashMap<String, PublicKey>(); // ClientName, Client public Key
    
    static {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);
    }

    public static void loadFromJSON(String username) {
        clientKeys = ManageJson.getUsersWithPublicKey(username);
        ChatGUI.loadOnlineClients(clientKeys.keySet());
        logger.info("Client Online List Added");
    }

    public static void addClient(String clientName, PublicKey publicKey) {
        clientKeys.put(clientName, publicKey);
        ChatGUI.addOnlineUser(clientName);
        logger.info("Added client: "+clientName);;
    }
    
    public static void removeClient(String clientName) {
        clientKeys.remove(clientName);
        ChatGUI.removeOnlineUser(clientName);
        logger.info("Removed client: "+clientName);;
    }

    public static void modifyPublicKey(String clientName, PublicKey publicKey) {
        clientKeys.put(clientName, publicKey);   
    }

    public static PublicKey getClientKey(String clientName){
        return clientKeys.get(clientName);
    }
}
