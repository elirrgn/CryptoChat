package chat.Client;

import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Class to manage client's output, sends messages
 */
public class OutputManager {
    private ObjectOutputStream out;

    /**
     * Constructor of the class
     * 
     * @param out client's ObjectOutputStream
     */
    public OutputManager(ObjectOutputStream out) {
        this.out = out;
    }
    
    /**
     * Sends messages with the ObjectOutputStream
     * 
     * @param msg message to send
     */
    public void sendMsg(String msg) {
        try {
            out.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
