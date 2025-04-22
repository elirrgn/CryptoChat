package chat.Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.config.Configurator;

import chat.Shared.ManageJson;

public class ClientList {
    private static final Logger logger = LogManager.getLogger(ClientList.class);

    private static HashMap<String, Socket> list = new HashMap<String, Socket>(); // nome -> Socket
    private static HashMap<String, ClientManager> managers = new HashMap<String,ClientManager>(); // nome -> manager
    
    static {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);
    }

    public synchronized static void add(Socket client, ObjectOutputStream out, ObjectInputStream in, String nome) {
        list.put(nome, client);
        try {
            managers.put(nome, new ClientManager(nome, client, out, in));
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info(nome+" joined the chat");
    }

    public synchronized static void sendDM(String nomeDest, String msg) throws IOException {
        managers.get(nomeDest).sendMsg(msg);
    }

    public synchronized static void sendAll(String msg) throws IOException {
        managers.forEach((k, value) -> {
            try {
                value.sendMsg(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public synchronized static void sendAll(String nome, String msg) throws IOException {
        managers.forEach((k, value) -> {
            if(!k.equals(nome)) {
                try {
                    value.sendMsg(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public synchronized static void removeClient(String nome) {
        list.remove(nome);
        managers.remove(nome);
        ManageJson.aggiungiChiavePubblica(nome, null);
        logger.info(nome+" left the chat");
    }

    public synchronized static Socket find(String nome) {
        return list.get(nome);
    }
}
