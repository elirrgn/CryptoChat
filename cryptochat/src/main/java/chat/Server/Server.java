package chat.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.config.Configurator;

import chat.Shared.ManageJson;


public class Server {
    private static final Logger logger = LogManager.getLogger(Server.class);

	private static final int PORT=8080;

    public static void main(String[] args) {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Waiting for clients on port: "+PORT);

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("Client connected");
                    ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                    String nome = SecureAuthenticateClient.SecureAuthentication(out, in);
                    if(nome != null) {
                        out.writeObject("/sendPublic");
                        String publicKey = (String) in.readObject();
                        // System.out.println(publicKey);
                        ManageJson.aggiungiChiavePubblica(nome, publicKey);
                        ClientList.sendAll(nome, "/connected;;"+nome+";;"+publicKey);
                        ClientList.add(clientSocket, out, in, nome);
                    } else {
                        logger.info("Client not authenticated");
                    }
                } catch (Exception e) {
                    System.err.println("Error handling connection: " + e.getMessage());
                }
            }
        } catch(Exception e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }
}
