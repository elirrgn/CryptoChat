package chat.Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import chat.Shared.PacketManager;

public class ClientManager implements Runnable {
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ClientManager(Socket socket, ObjectOutputStream out, ObjectInputStream in) throws IOException {
        this.in = in;
        this.out = out;

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
                System.out.println(msg);
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
