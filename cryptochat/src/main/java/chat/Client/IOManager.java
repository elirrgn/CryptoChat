package chat.Client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class IOManager {
    private Socket socket;
    private OutputManager outputManager;
    private InputManager inputManager;
    private KeyPair keys;

    public IOManager(Socket clientSocket, ObjectOutputStream out, ObjectInputStream in) {
        try {
            this.socket = clientSocket;
            this.keys = RSAUtils.generateKeyPair();
            ServerCommandManager.addIOManager(this);
            
            this.outputManager = new OutputManager(out);
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
        outputManager.sendMsg(msg);
    }
}
