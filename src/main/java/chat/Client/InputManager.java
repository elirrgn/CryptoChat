package chat.Client;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import chat.Shared.AES;
import chat.Shared.PacketManager;

public class InputManager implements Runnable {
    private static final Logger logger = LogManager.getLogger(InputManager.class);
    static {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);
    }

    private ObjectInputStream in;
    private IOManager ioManager;

    public InputManager(ObjectInputStream in, IOManager ioManager) throws IOException {
        this.in = in;
        this.ioManager = ioManager;

        new Thread(this).start();
    }

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

                                ChatGUI.appendMessage("[DIRECT MESSAGE: "+sender+"] "+decryptedMsg, "#9B59B6");
                            } catch(Exception e) {
                                logger.error("Error decrypting direct message from {}: {}", sender, e.getMessage());
                            }
                        }
                    }
                } else {
                    ChatGUI.appendMessage(msg, "#7F8C8D");
                }
            } catch (ClassNotFoundException | IOException e) {
                logger.error("Error while processing message or server disconnected: {}", e.getMessage());
                ChatGUI.serverDisconnected();
                return;
            }
        }
    }
    
}
