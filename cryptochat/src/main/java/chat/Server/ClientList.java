package chat.Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

public class ClientList {
    private static HashMap<String, Socket> list = new HashMap<String, Socket>();
    private static HashMap<String, ClientManager> managers = new HashMap<String,ClientManager>();
    
    public static void add(Socket client, ObjectOutputStream out, ObjectInputStream in, String nome) {
        list.put(nome, client);
        try {
            managers.put(nome, new ClientManager(client, out, in));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendDM(String nomeDest, String msg) throws IOException {
        managers.get(nomeDest).sendMsg(msg);
    }

    public static void sendAll(String msg) throws IOException {
        for (ClientManager manager : managers.values()) {
            manager.sendMsg(msg);
        }
    }
}
