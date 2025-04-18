package chat.Client;

import java.security.PublicKey;
import java.util.HashMap;

public class OnlineList {
    private static HashMap<String, PublicKey> clientKeys = new HashMap<String, PublicKey>(); // ClientName, Client public Key
    
    public static void addClient(String clientName, PublicKey publicKey) {
        clientKeys.put(clientName, publicKey);
    }

    public static void removeClient(String clientName) {
        clientKeys.remove(clientName);
    }

    public static void modifyPublicKey(String clientName, PublicKey publicKey) {
        clientKeys.put(clientName, publicKey);   
    }

    public static PublicKey getClientKey(String clientName){
        return clientKeys.get(clientName);
    }
}
