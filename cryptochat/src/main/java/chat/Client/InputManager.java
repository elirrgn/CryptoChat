package chat.Client;

import java.io.IOException;
import java.io.ObjectInputStream;

import chat.Shared.PacketManager;

public class InputManager implements Runnable {
    private ObjectInputStream in;


    public InputManager(ObjectInputStream in) throws IOException {
        this.in = in;

        new Thread(this).start();
    }

    @Override
    public void run() {
        while(true) {
            try {
                String msg = in.readObject().toString();
                if(msg.startsWith("/")) {
                    ServerCommandManager.manageCommand(msg);
                } else if(PacketManager.checkPacketFormat(msg)) {
                    // Manage Chat Messages
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
    }
    
}
