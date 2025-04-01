package chat.Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import chat.Shared.PacketManager;

public class ClientManager implements Runnable {
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ClientManager(Socket socket) throws IOException {
        this.in = new ObjectInputStream(socket.getInputStream());
        this.out = new ObjectOutputStream(socket.getOutputStream());

        new Thread(this).start();
    }

    public void sendMsg(String msg) throws IOException {
        out.writeObject(msg);
    }

    @Override
    public void run() {
        while(true) {
            try {
                String msg = (String) in.readObject();
                
                if(PacketManager.checkPacketFormat(msg)) {
                    String dest = PacketManager.getPacketDest(msg);
                    if(dest != "all") {
                        ClientList.sendDM(dest, msg);
                    } else {
                        ClientList.sendAll(msg);
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
}
