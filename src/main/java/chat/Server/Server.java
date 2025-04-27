package chat.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.config.Configurator;

import chat.Shared.ManageJson;


/**
 * Main Server class, client connection management
 */
public class Server {
    private static final Logger logger = LogManager.getLogger(Server.class);

    private static final int PORT = 8080;

    public static void main(String[] args) {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Waiting for clients on port: " + PORT);

            // runs when I do ctrl+C on the console, closes correctly all the Streams
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Server shutting down...");
                ManageJson.deleteOnlinePublicKeys();
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    logger.error("Error handling server socket: " + e.getMessage());
                }
            }));

            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Client connected");

                // Start a new thread to authenticate the client
                new Thread(() -> handleClientAuthentication(clientSocket)).start();
            }
        } catch (Exception e) {
            logger.error("Error handling server socket: " + e.getMessage());
        }
    }

    /**
     * Manages authentication with client, adds public key to the JSON file and communicate to other clients
     * 
     * @param clientSocket the socket of the connected client
     */
    private static void handleClientAuthentication(Socket clientSocket) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            SecureAuthenticateClient auth = new SecureAuthenticateClient();
            String nome = auth.SecureAuthentication(out, in);
            if (nome != null) {
                out.writeObject("/sendPublic");
                out.flush();
                String publicKey = (String) in.readObject();
                ManageJson.addOrEditPublicKey(nome, publicKey);
                ClientList.sendAll(nome, "/connected;;" + nome + ";;" + publicKey);
                ClientList.add(clientSocket, out, in, nome);
                logger.info("Client " + nome + " authenticated and added to ClientList");
            } else {
                logger.info("Client failed to authenticate, closing connection");
                clientSocket.close();
            }
        } catch (Exception e) {
            logger.error("Error during client authentication: " + e.getMessage());
            try {
                clientSocket.close();
            } catch (IOException ioException) {
                return;
            }
        }
    }
}
