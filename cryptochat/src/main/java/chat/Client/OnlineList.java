package chat.Client;

import java.util.HashMap;

public class OnlineList {
    private static HashMap<String, String> clientKeys = new HashMap<String, String>(); // ClientName, Client public Key
    
    public static void addOnlineClient(String clientName, String publicKey) {
        clientKeys.put(clientName, publicKey);
    }

    public static void removeClient(String clientName) {
        clientKeys.remove(clientName);
    }

    public static void modifyPublicKey(String clientName, String publicKey) {
        clientKeys.put(clientName, publicKey);   
    }

    public static String getClientKey(String clientName){
        return clientKeys.get(clientName);
    }
}
