package chat.Client;

public class ServerCommandManager {
    private static IOManager ioManager;
    
    public static void addIOManager(IOManager io) {
        ioManager = io;
    }

    public static void manageCommand(String msg) {
        String[] splitMsg = msg.split(";;");
        switch (splitMsg[0]) {
            case "/sendPublic":
                ioManager.sendToServer(RSAUtils.publicKeyToString(ioManager.getPublicKey()));
                break;
            case "/connected":
                try {
                    OnlineList.addClient(splitMsg[1], RSAUtils.stringToPublicKey(splitMsg[2]));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                ChatGUI.appendMessage(splitMsg[1]+" joined the chat!", "#7F8C8D");
                break;
            case "/disconnected":
                OnlineList.removeClient(splitMsg[1]);
                ChatGUI.appendMessage(splitMsg[1]+" left the chat!", "#7F8C8D");
                break;
            default:
                break;
        }
    }

}
