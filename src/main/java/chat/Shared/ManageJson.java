package chat.Shared;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.util.HashMap;

import org.json.JSONObject;

import chat.Client.RSAUtils;

public class ManageJson {
    private static final String USERS_FILE = "users.json";

    public synchronized static JSONObject caricaUtenti() {
        try {
            if (!Files.exists(Paths.get(USERS_FILE))) {
                return new JSONObject();
            }

            String content = new String(Files.readAllBytes(Paths.get(USERS_FILE)));
            System.out.println(new JSONObject(content));
            return new JSONObject(content);

        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    public synchronized static void salvaUtenti(JSONObject utenti) {
        try (FileWriter file = new FileWriter(USERS_FILE)) {
            file.write(utenti.toString(4)); // formattato
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static boolean aggiungiChiavePubblica(String username, String publicKey) {
        try {
            JSONObject utenti = caricaUtenti();

            if (!utenti.has(username)) {
                System.err.println("Utente non trovato: " + username);
                return false;
            }

            JSONObject userObj = utenti.getJSONObject(username);
            userObj.put("publicKey", publicKey); // Add or update publicKey
            utenti.put(username, userObj); // Not strictly needed, but safe

            salvaUtenti(utenti);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized static HashMap<String, PublicKey> getUtentiConPublicKey(String clientName) {
        HashMap<String, PublicKey> utentiConChiave = new HashMap<>();
        JSONObject utenti = caricaUtenti();
        //System.out.println(utenti);
    
        for (String username : utenti.keySet()) {
            if (!username.equals(clientName)) { // Exclude the requesting client
                System.out.println(username);
                JSONObject userInfo = utenti.optJSONObject(username); // Get user info
                if (userInfo != null && userInfo.has("publicKey")) {
                    // Check if the user has a publicKey
                    String publicKey = userInfo.optString("publicKey", null);
                    if (publicKey != null && !publicKey.isEmpty()) {
                        try {
                            // Convert the publicKey string to a PublicKey object and add it to the map
                            PublicKey pubKey = RSAUtils.stringToPublicKey(publicKey);
                            utentiConChiave.put(username, pubKey);
                        } catch (Exception e) {
                            // Handle any exceptions that might arise during publicKey conversion
                            System.err.println("Error converting public key for " + username + ": " + e.getMessage());
                        }
                    }
                }
            }
        }
    
        return utentiConChiave;
    }
    
}
