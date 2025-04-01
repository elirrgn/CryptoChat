package chat.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	private static final int PORT=1234;

    public static void main(String[] args) {
        LogsManager.setUp();
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            LogsManager.INFO("Waiting for clients on port: "+PORT);

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) 
                {
                    LogsManager.INFO("Client connected");
                    //ClientList.add(clientSocket);
                    String nome = SecureAuthenticateClient.SecureAuthentication(new ObjectOutputStream(clientSocket.getOutputStream()), new ObjectInputStream(clientSocket.getInputStream()));
                    LogsManager.INFO("Client authenticated as "+nome);
                    if(nome != null) {
                        ClientList.add(clientSocket, nome);
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
