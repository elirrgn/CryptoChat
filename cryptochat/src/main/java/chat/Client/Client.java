package chat.Client;

import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {
    private static InetSocketAddress host;
    private static final String ADDRESS = "localhost";
	private static final int PORT=1234;

    public static void main(String[] args) {
		try {
			host = new InetSocketAddress(ADDRESS, PORT);
		} catch (Exception e) {
			System.out.println("Host ID not found!");
			System.exit(1);
		}

    }

    public static void chatWithServer() {
        Socket connection = new Socket();
    }
}
