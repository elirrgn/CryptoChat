package chat.Client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Scanner;

public class OutputManager {
    private ObjectOutputStream out;
    private Scanner input;
    private IOManager ioManager;

    public OutputManager(ObjectOutputStream out, IOManager ioManager) throws IOException {
        this.out = out;
        this.ioManager = ioManager;
        this.input = new Scanner(System.in);
    }
    
    public void sendMsg(String msg) {
        try {
            out.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
