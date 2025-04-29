package chat.Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.config.Configurator;

import chat.Shared.ManageJson;

/**
 * Manages the connected clients in the chat system.
 * Provides methods to add, remove, find clients, and send messages either globally or directly.
 */
public class ClientList {
    private static final Logger logger = LogManager.getLogger(ClientList.class);

    private static HashMap<String, Socket> list = new HashMap<String, Socket>(); // name -> Socket
    private static HashMap<String, ClientManager> managers = new HashMap<String,ClientManager>(); // name -> manager
    
    static {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.DEBUG);
    }

    /**
     * Adds a new client to the chat system and initializes their manager.
     *
     * @param client the socket of the client
     * @param out client's ObjectOutputStream
     * @param in client's ObjectInputStream
     * @param name client's username
     */
    public synchronized static void add(Socket client, ObjectOutputStream out, ObjectInputStream in, String name) {
        list.put(name, client);
        try {
            managers.put(name, new ClientManager(name, client, out, in));
            logger.info(name+" joined the chat");
        } catch (IOException e) {
            logger.error("Error adding client", e);
        }
    }

    /**
     * Sends a direct message to a specific client.
     *
     * @param destinationName the username of the destination
     * @param msg the message to send
     * @throws IOException if an error occurs while sending the message
     */
    public synchronized static void sendDM(String destinationName, String msg) throws IOException {
        managers.get(destinationName).sendMsg(msg);
    }

    /**
     * Sends a message to all connected clients.
     *
     * @param msg the message to broadcast
     * @throws IOException if an error occurs while sending the message
     */
    public synchronized static void sendAll(String msg) throws IOException {
        managers.forEach((k, value) -> {
            try {
                value.sendMsg(msg);
            } catch (IOException e) {
                logger.error("Error sending message to " + k, e);
            }
        });
    }
    
    /**
     * Sends a message to all connected clients except the sender.
     *
     * @param senderName the name of the client who sent the message
     * @param msg the message to broadcast
     * @throws IOException if an error occurs while sending the message
     */
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
    
    /**
     * Removes a client from the chat system and removes the publickey from the JSON file.
     *
     * @param name the name of the client to remove
     */
    public synchronized static void removeClient(String name) {
        list.remove(name);
        managers.remove(name);
        ManageJson.addOrEditPublicKey(name, null);
        logger.info(name+" left the chat");
    }


    /**
     * Finds the socket connection associated with a client name.
     *
     * @param name the name of the client
     * @return the socket if found, or null if not found
     */
    public synchronized static Socket find(String name) {
        return list.get(name);
    }
}
