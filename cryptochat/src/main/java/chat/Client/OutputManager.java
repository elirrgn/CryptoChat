package chat.Client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.NoSuchElementException;
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
            try {
                String msg = input.nextLine();
                sendMsg(msg);
                if (!msg.startsWith("/")) {
                    
                }
            } catch(NoSuchElementException e) {
                return;
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
