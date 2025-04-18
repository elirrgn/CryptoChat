package chat.Shared;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONObject;

public class ManageJson {
    private static final String USERS_FILE = "users.json";

    public static JSONObject caricaUtenti() {
        try {
            if (!Files.exists(Paths.get(USERS_FILE))) {
                return new JSONObject();
            }

            String content = new String(Files.readAllBytes(Paths.get(USERS_FILE)));
            return new JSONObject(content);

        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    public static void salvaUtenti(JSONObject utenti) {
        try (FileWriter file = new FileWriter(USERS_FILE)) {
            file.write(utenti.toString(4)); // formattato
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean aggiungiChiavePubblica(String username, String publicKey) {
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
}
