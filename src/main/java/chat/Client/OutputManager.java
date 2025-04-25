package chat.Client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.crypto.SecretKey;

import chat.Shared.AES;
import chat.Shared.PacketManager;

public class OutputManager {
    private ObjectOutputStream out;
    private Scanner input;
    private IOManager ioManager;

    public OutputManager(ObjectOutputStream out, IOManager ioManager) throws IOException {
        this.out = out;
        this.ioManager = ioManager;
        this.input = new Scanner(System.in);

        //new Thread(this).start();
    }

    /*@Override
    public void run() {
        while(true) {
            try {
                String msg = input.nextLine();
                if(!msg.startsWith("/")) {
                    try {
                        SecretKey aesKey = AES.generateAESKey();
                        String stringAesKey = AES.secretKeyToString(aesKey);
                        String encryptedMsg = AES.encrypt(msg, aesKey);
                        String encryptedAesKey = RSAUtils.encrypt(encryptedMsg, ioManager.getPrivateKey());
                        String packet = PacketManager.createMsgPacket(ioManager.getUsername(), "all", encryptedMsg, encryptedAesKey);
                        sendMsg(packet);
                    } catch (Exception e) {

                    }
                } else {
                    if(msg.startsWith("/DM")) {
                        
                    }
                }
            } catch(NoSuchElementException e) {
                return;
            }
        }
    }
    */
    
    public void sendMsg(String msg) {
        try {
            out.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
