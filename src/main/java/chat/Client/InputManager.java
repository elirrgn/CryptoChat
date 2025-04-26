package chat.Client;

import java.io.IOException;
import java.io.ObjectInputStream;

import chat.Shared.AES;
import chat.Shared.PacketManager;

public class InputManager implements Runnable {
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
                    ServerCommandManager.manageCommand(msg);
                } else if(PacketManager.checkPacketFormat(msg)) {
                    if(PacketManager.getPacketDest(msg).equals("all")) {
                        String sender = PacketManager.getPacketSrc(msg);
                        if(OnlineList.getClientKey(sender) != null) {
                            try {
                                String decryptedaes = RSAUtils.decryptWithPublicKey(PacketManager.getPacketKey(msg), OnlineList.getClientKey(sender));
                                String decryptedMsg = AES.decrypt(PacketManager.getPacketMsg(msg), AES.stringToSecretKey(decryptedaes));
                                ChatGUI.appendMessage("["+sender+"] "+decryptedMsg);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }else {
                        String sender = PacketManager.getPacketSrc(msg);
                        if(OnlineList.getClientKey(sender) != null) {
                            try {
                                String firstDecryptedaes = RSAUtils.decryptWithPublicKey(PacketManager.getPacketKey(msg), OnlineList.getClientKey(sender));
                                String secondDecryptedaes = RSAUtils.decryptWithPrivateKey(firstDecryptedaes, ioManager.getPrivateKey());
                                String decryptedMsg = AES.decrypt(PacketManager.getPacketMsg(msg), AES.stringToSecretKey(secondDecryptedaes));
                                ChatGUI.appendMessage("[DIRECT MESSAGE: "+sender+"] "+decryptedMsg, "#9B59B6");
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                ChatGUI.serverDisconnected();
                return;
            }
        }
    }
    
}
