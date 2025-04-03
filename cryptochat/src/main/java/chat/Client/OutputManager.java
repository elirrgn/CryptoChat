package chat.Client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class OutputManager implements Runnable {
    private Socket socket;
    private ObjectOutputStream out;
    private Scanner input;


    public OutputManager(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());

        new Thread(this).start();
    }

    @Override
    public void run() {
        while(true) {
            input.nextLine();
            
        }
    }
    
}
