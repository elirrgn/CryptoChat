package chat.Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import chat.Shared.AES;
import chat.Shared.PacketManager;

public class IOManager {
    private Socket socket;
    private String username;
    private OutputManager outputManager;
    private InputManager inputManager;
    private KeyPair keys;

    public IOManager(Socket clientSocket, ObjectOutputStream out, ObjectInputStream in, String username) {
        try {
            this.username = username;
            this.socket = clientSocket;
            this.keys = RSAUtils.generateKeyPair();
            OnlineList.loadFromJSON(username);
            ServerCommandManager.addIOManager(this);
            
            this.outputManager = new OutputManager(out, this);
            this.inputManager = new InputManager(in);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PublicKey getPublicKey() {
        return keys.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return keys.getPrivate();
    }

    public void sendMsg(String msg) {
        if(!msg.startsWith("/")) {
            try {
                SecretKey aesKey = AES.generateAESKey();
                String stringAesKey = AES.secretKeyToString(aesKey);
                String encryptedMsg = AES.encrypt(msg, aesKey);
                String encryptedAesKey = RSAUtils.encrypt(stringAesKey, this.getPrivateKey());
                String packet = PacketManager.createMsgPacket(this.username, "all", encryptedMsg, encryptedAesKey);
                outputManager.sendMsg(packet);
            } catch (Exception e) {

            }
        } else {
            if(msg.startsWith("/DM")) {
                
            }
        }
    }

    public void sendToServer(String msg) {
        outputManager.sendMsg(msg);
    }

    public String getUsername() {
        return this.username;
    }
}
