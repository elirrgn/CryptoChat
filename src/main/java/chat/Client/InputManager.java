package chat.Client;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import chat.Shared.AES;
import chat.Shared.PacketManager;

/**
 * Class that manages the input stream, decrypts messages and manages them.
 */
public class InputManager implements Runnable {
    private static final Logger logger = LogManager.getLogger(InputManager.class);
    
    static {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.DEBUG);
    }

    private ObjectInputStream in;
    private IOManager ioManager;

    /**
     * Constructor of the class
     * 
     * @param in client's ObjectInputStram
     * @param ioManager client's IOManager
     * @throws IllegalThreadStateException on Thread.start
     */
    public InputManager(ObjectInputStream in, IOManager ioManager) throws IllegalThreadStateException {
        this.in = in;
        this.ioManager = ioManager;

        new Thread(this).start();
    }

    /**
     * The main loop for processing incoming messages from the server.
     * 
     * This method continuously listens for incoming messages from the server and handles them based on their format.
     * It differentiates between command messages, broadcast messages, and direct messages. Commands are managed by 
     * the ServerCommandManager, while messages are decrypted and displayed in the chat GUI. 
     * The method handles both broadcast and direct messages, including decryption using RSA and AES encryption.
     * 
     * If the message format is invalid or an error occurs during message decryption, the method logs an error.
     * If the server disconnects or an I/O error occurs, the method updates the GUI and terminates the loop.
     */
    @Override
    public void run() {
        while(true) {
            try {
                String msg = in.readObject().toString();

                if(msg.startsWith("/")) {
                    logger.info("Command detected: {}", msg);
                    ServerCommandManager.manageCommand(msg);
                } else if(PacketManager.checkPacketFormat(msg)) {
                    if(PacketManager.getPacketDest(msg).equals("all")) {
                        String sender = PacketManager.getPacketSrc(msg);
                        logger.info("Broadcast message from: {}", sender);

                        if(OnlineList.getClientKey(sender) != null) {
                            try {
                                String decryptedaes = RSAUtils.decryptWithPublicKey(PacketManager.getPacketKey(msg), OnlineList.getClientKey(sender));
                                String decryptedMsg = AES.decrypt(PacketManager.getPacketMsg(msg), AES.stringToSecretKey(decryptedaes));
                                logger.info("Decrypted message: {}", decryptedMsg);

                                ChatGUI.appendMessage("["+sender+"] "+decryptedMsg);
                            } catch (Exception e) {
                                logger.error("Error decrypting message from {}: {}", sender, e.getMessage());
                            }
                        }
                    }else {
                        String sender = PacketManager.getPacketSrc(msg);
                        logger.info("Direct message from: {}", sender);

                        if(OnlineList.getClientKey(sender) != null) {
                            try {
                                String firstDecryptedaes = RSAUtils.decryptWithPublicKey(PacketManager.getPacketKey(msg), OnlineList.getClientKey(sender));
                                String secondDecryptedaes = RSAUtils.decryptWithPrivateKey(firstDecryptedaes, ioManager.getPrivateKey());
                                String decryptedMsg = AES.decrypt(PacketManager.getPacketMsg(msg), AES.stringToSecretKey(secondDecryptedaes));
                                logger.info("Decrypted direct message: {}", decryptedMsg);

                                ChatGUI.appendMessage("[DIRECT MESSAGE: "+sender+"] "+decryptedMsg, "#9B59B6");// Color purple
                            } catch(Exception e) {
                                logger.error("Error decrypting direct message from {}: {}", sender, e.getMessage());
                            }
                        }
                    }
                } else {
                    ChatGUI.appendMessage(msg, "#7F8C8D"); //gray for server messages
                }
            } catch (ClassNotFoundException | IOException e) {
                logger.error("Error while processing message or server disconnected: {}", e.getMessage());
                ChatGUI.serverDisconnected();
                return;
            }
        }
    }
    
}
