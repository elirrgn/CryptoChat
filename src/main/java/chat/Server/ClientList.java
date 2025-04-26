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

    private static HashMap<String, Socket> list = new HashMap<String, Socket>(); // name -> Socket
    private static HashMap<String, ClientManager> managers = new HashMap<String,ClientManager>(); // name -> manager
    
    static {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);
    }

    public synchronized static void add(Socket client, ObjectOutputStream out, ObjectInputStream in, String name) {
        list.put(name, client);
        try {
            managers.put(name, new ClientManager(name, client, out, in));
            logger.info(name+" joined the chat");
        } catch (IOException e) {
            logger.error("Error adding client", e);
        }
    }

    public synchronized static void sendDM(String destinationName, String msg) throws IOException {
        managers.get(destinationName).sendMsg(msg);
    }

    public synchronized static void sendAll(String msg) throws IOException {
        managers.forEach((k, value) -> {
            try {
                value.sendMsg(msg);
            } catch (IOException e) {
                logger.error("Error sending message to " + k, e);
            }
        });
    }

    public synchronized static void sendAll(String senderName, String msg) throws IOException {
        managers.forEach((k, value) -> {
            if(!k.equals(senderName)) {
                try {
                    value.sendMsg(msg);
                } catch (IOException e) {
                    logger.error("Error sending message to " + k, e);
                }
            }
        });
    }

    public synchronized static void removeClient(String name) {
        list.remove(name);
        managers.remove(name);
        ManageJson.addOrEditPublicKey(name, null);
        logger.info(name+" left the chat");
    }

    public synchronized static Socket find(String name) {
        return list.get(name);
    }
}
