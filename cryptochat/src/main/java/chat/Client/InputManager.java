package chat.Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import chat.Shared.PacketManager;

public class InputManager implements Runnable {
    private Socket socket;
    private ObjectInputStream in;


    public InputManager(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new ObjectInputStream(socket.getInputStream());

        new Thread(this).start();
    }

    @Override
    public void run() {
        while(true) {
            try {
                String msg = in.readObject().toString();
                if(msg.startsWith("/")) {
                    // Manage Server Messages
                }

                if(PacketManager.checkPacketFormat(msg)) {
                    // Manage Chat Messages
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
    }
    
}
