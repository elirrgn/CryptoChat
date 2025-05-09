package chat.Client;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

/**
 * Utility class that manages Server Commands
 */
public class ServerCommandManager {
    private static final Logger logger = LogManager.getLogger(ServerCommandManager.class);
    static {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.DEBUG);
    }

    private static IOManager ioManager;
    
    /**
     * Adds the ioManager to the class
     * @param io client's IOManager
     */
    public static void addIOManager(IOManager io) {
        ioManager = io;
    }

    /**
     * Manages Server Commands: public key request and connections/disconnections
     * 
     * @param msg the server message
     */
    public static void manageCommand(String msg) {
        String[] splitMsg = msg.split(";;");

        switch (splitMsg[0]) {
            case "/sendPublic":
                logger.info("Sending public key to server...");
                ioManager.sendToServer(RSAUtils.publicKeyToString(ioManager.getPublicKey()));
                break;
            case "/connected":
                try {
                    logger.info("Client {} connected.", splitMsg[1]);
                    logger.debug("Adding Client {} to OnlineList...", splitMsg[1]);
                    OnlineList.addClient(splitMsg[1], RSAUtils.stringToPublicKey(splitMsg[2]));
                } catch (Exception e) {
                    logger.error("Error processing /connected command for client: " + splitMsg[1], e);
                }
                ChatGUI.appendMessage(splitMsg[1]+" joined the chat!", "#7F8C8D");
                break;
            case "/disconnected":
                logger.info("Client {} disconnected", splitMsg[1]);
                logger.info("Removing client {} from OnlineList", splitMsg[1]);
                OnlineList.removeClient(splitMsg[1]);
                ChatGUI.appendMessage(splitMsg[1]+" left the chat!", "#7F8C8D");
                break;
            default:
                logger.warn("Unknown command received: {}", splitMsg[0]);
                break;
        }
    }

}
