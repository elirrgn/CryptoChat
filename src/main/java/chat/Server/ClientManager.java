package chat.Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import chat.Shared.PacketManager;

/**
 * Client that manages a client input and output stream
 */
public class ClientManager implements Runnable {
    private static final Logger logger = LogManager.getLogger(ClientManager.class);
    static {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);
    }

    private String name;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    /**
     * Constructor of the class
     * 
     * @param name the client's username
     * @param socket the client's socket
     * @param out client's ObjectOutputStream
     * @param in client's ObjectInputStream
     * @throws IOException if an error occurs during communication
     */
    public ClientManager(String name, Socket socket, ObjectOutputStream out, ObjectInputStream in) throws IOException {
        this.name = name;
        this.in = in;
        this.out = out;

        logger.info("Started ClientManager thread for user: " + name);

        out.writeObject("""
Welcome to CryptoChat! Here’s what you can do:

-Send a global message: Type your message in the input field and press Enter or click the Send button to send it to everyone in the chat.
-Send a direct message (DM): Double-click a username from the Online Users list. Your message field will be prefilled with \"/DM;;username;;\". Type your message and press -Enter or click Send to send a direct message.
-Logout/Disconnect: To exit, close the window.

Commands:
/help - Displays this help guide.
/DM;;[username];;[message] - Send a direct message to a specific user.
                            """);
        new Thread(this).start();
    }

    /**
     * Sends a message to the client
     * 
     * @param msg message to send
     * @throws IOException if an error occurs during communication
     */
    public void sendMsg(String msg) throws IOException {
        out.writeObject(msg);
    }

    /**
     * Listens for incoming messages and handles commands or message forwarding.
     * Manages client disconnection on errors.
     */
    @Override
    public void run() {
        while(true) {
            try {
                String msg = (String) in.readObject();
                logger.debug("Received message: " + msg);
                if(msg.equalsIgnoreCase("/help")) {
                    this.out.writeObject("""
Welcome to CryptoChat! Here’s what you can do:

-Send a global message: Type your message in the input field and press Enter or click the Send button to send it to everyone in the chat.
-Send a direct message (DM): Double-click a username from the Online Users list. Your message field will be prefilled with \"/DM;;username;;\". Type your message and press -Enter or click Send to send a direct message.
-Logout/Disconnect: To exit, close the window.

Commands:
/help - Displays this help guide.
/DM;;[username];;[message] - Send a direct message to a specific user.
                            """);
                } else if(PacketManager.checkPacketFormat(msg)) {
                    String src = PacketManager.getPacketSrc(msg);
                    String dest = PacketManager.getPacketDest(msg);
                    if(!dest.equals("all")) {
                        ClientList.sendDM(dest, msg);
                        logger.info(PacketManager.getPacketSrc(msg)+" message to " + PacketManager.getPacketDest(msg));
                    } else {
                        ClientList.sendAll(src, msg);
                        logger.info(PacketManager.getPacketSrc(msg)+" message to all");
                    }
                } else {
                    logger.warn("Invalid packet format received from " + name + ": " + msg);
                }
            } catch (Exception e) {
                try {
                    out.close();
                    in.close();
                    ClientList.sendAll(name, "/disconnected;;"+ name);
                    ClientList.removeClient(name);
                    return;
                } catch (IOException e1) {
                    logger.error("Failed to close streams for " + name, e1);
                    e1.printStackTrace();
                }
            }
        }
    }

    
}
