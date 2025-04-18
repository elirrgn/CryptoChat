package chat.Client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class OutputManager implements Runnable {
    private ObjectOutputStream out;
    private Scanner input;

    public OutputManager(ObjectOutputStream out) throws IOException {
        this.out = out;
        this.input = new Scanner(System.in);

        new Thread(this).start();
    }

    @Override
    public void run() {
        while(true) {
            String msg = input.nextLine();
            sendMsg(msg);
            if (!msg.startsWith("/")) {
                
            }
        }
    }
    
    public void sendMsg(String msg) {
        try {
            out.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
