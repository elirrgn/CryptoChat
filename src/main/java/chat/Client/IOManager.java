package chat.Client;

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
            this.inputManager = new InputManager(in, this);

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

    public String sendMsg(String msg) {
        if(!msg.startsWith("/")) {
            try {
                SecretKey aesKey = AES.generateAESKey();
                String stringAesKey = AES.secretKeyToString(aesKey);
                String encryptedMsg = AES.encrypt(msg, aesKey);
                String encryptedAesKey = RSAUtils.encryptWithPrivateKey(stringAesKey, this.getPrivateKey());
                String packet = PacketManager.createMsgPacket(this.username, "all", encryptedMsg, encryptedAesKey);
                outputManager.sendMsg(packet);
                return null;
            } catch (Exception e) {
                return "Error! Message not sent";
            }
        } else {
            if(msg.startsWith("/DM")) {
                try {
                    String[] command = msg.split(";;");
                    String dest = command[1];
                    String msgString = command[2];

                    SecretKey aesKey = AES.generateAESKey();
                    String stringAesKey = AES.secretKeyToString(aesKey);
                    String encryptedMsg = AES.encrypt(msgString, aesKey);

                    if(OnlineList.getClientKey(dest) != null) {
                        String firstEncryptedAesKey = RSAUtils.encryptWithPublicKey(stringAesKey, OnlineList.getClientKey(dest));

                        String secondEncryptedAesKey = RSAUtils.encryptWithPrivateKey(firstEncryptedAesKey, this.getPrivateKey());

                        String packet = PacketManager.createMsgPacket(this.username, dest, encryptedMsg, secondEncryptedAesKey);
                        outputManager.sendMsg(packet);
                        return null;
                    } else {
                        return "Client not found! Message not sent";
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                    return "Message format error, message not sent!";
                }
            }
        }
        return "Error!";
    }

    public void sendToServer(String msg) {
        outputManager.sendMsg(msg);
    }

    public String getUsername() {
        return this.username;
    }
}
