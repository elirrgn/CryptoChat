package chat.Server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;

import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import chat.Shared.AES;
import chat.Shared.DHKeyExchange;
import chat.Shared.ManageJson;

public class SecureAuthenticateClient {
    private static BigInteger sharedKey;
    private static SecretKey aesKey;

    public static String SecureAuthentication(ObjectOutputStream out, ObjectInputStream in) {
        try {
            keyExchange(out, in);
            deriveAESKey();
            return authentication(out, in);
        } catch(Exception e) {
            System.out.println(e.getLocalizedMessage());
            return null;
        }
    }

    private static void keyExchange(ObjectOutputStream out, ObjectInputStream in) throws Exception {
        sharedKey = DHKeyExchange.serverSideSharedKeyCreation(out, in);
    }

    private static void deriveAESKey() throws NoSuchAlgorithmException {
        aesKey = AES.deriveAESKey(sharedKey.toByteArray());
    }

    private static String authentication(ObjectOutputStream out, ObjectInputStream in) throws Exception {
        boolean result;
        do{
            String message = """ 
            Inserisci il comando corretto:
                R: Registrati
                L: Login
                E: ESCI
            """;
            String encryptedResponse = AES.encrypt(message, aesKey);
            out.writeObject(encryptedResponse);
            out.flush();
            String encryptedMessage = (String) in.readObject();
            String command = AES.decrypt(encryptedMessage, aesKey);
            
            String username;
            String psw;
            switch(command.toUpperCase()) {
                case "R": 
                        message = "Inserisci l'username!";
                        encryptedResponse = AES.encrypt(message, aesKey);
                        out.writeObject(encryptedResponse);
                        out.flush();
                
                        encryptedMessage = (String) in.readObject();
                        username = AES.decrypt(encryptedMessage, aesKey);
                        
                        
                        message = "Inserisci la password!";
                        encryptedResponse = AES.encrypt(message, aesKey);
                        out.writeObject(encryptedResponse);
                        out.flush();
                
                        encryptedMessage = (String) in.readObject();
                        psw = AES.decrypt(encryptedMessage, aesKey);
                        String hashedPsw = BCrypt.hashpw(psw, BCrypt.gensalt(12)); // 12 fattore abbastanza sicuro e veloce
                        result = registraUtente(username, hashedPsw);
                        if(!result) {
                            out.writeObject(AES.encrypt("/authenticationFailed", aesKey));
                            out.flush();
                        } else {
                            out.writeObject(AES.encrypt("/authenticationCorrect", aesKey));
                            out.flush();
                            return username;
                        }
                        break;
                case "L": 
                        message = "Inserisci l'username!";
                        encryptedResponse = AES.encrypt(message, aesKey);
                        out.writeObject(encryptedResponse);
                        out.flush();
                        encryptedMessage = (String) in.readObject();
                        username = AES.decrypt(encryptedMessage, aesKey);
                        
                        message = "Inserisci la password!";
                        encryptedResponse = AES.encrypt(message, aesKey);
                        out.writeObject(encryptedResponse);
                        out.flush();
                        encryptedMessage = (String) in.readObject();
                        psw = AES.decrypt(encryptedMessage, aesKey);


                        result = login(username, psw);
                        if(!result) {
                            out.writeObject(AES.encrypt("/authenticationFailed", aesKey));
                            out.flush();
                        } else {
                            out.writeObject(AES.encrypt("/authenticationCorrect", aesKey));
                            out.flush();
                            return username;
                        }
                        break;
                case "E": return null;
                default: result = false; 
            }
        } while (!result);
        return null;
    }

    private static boolean registraUtente(String username, String hashedPassword) {
        try {
            JSONObject utenti = ManageJson.caricaUtenti();
    
            if (utenti.has(username)) {
                return false; // utente già registrato
            }
            
            // Crea oggetto utente
            JSONObject userObj = new JSONObject();
            userObj.put("hash", hashedPassword);
    
            // Salva nel file
            utenti.put(username, userObj);
            ManageJson.salvaUtenti(utenti);
            return true;
    
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean login(String username, String password) {
        try {
            JSONObject utenti = ManageJson.caricaUtenti();
    
            if (!utenti.has(username)) {
                return false; // utente non esiste
            }
    
            JSONObject userObj = utenti.getJSONObject(username);
            String hashSalvato = userObj.getString("hash");
    
            return BCrypt.checkpw(password, hashSalvato);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
