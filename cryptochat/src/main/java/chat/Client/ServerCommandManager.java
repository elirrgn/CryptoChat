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
                ioManager.sendMsg(RSAUtils.publicKeyToString(ioManager.getPublicKey()));
                break;
            case "/connected":
                OnlineList.addClient(splitMsg[1], RSAUtils.stringToPublicKey(splitMsg[2]));
                break;
            case "/disconnected":
                OnlineList.removeClient(splitMsg[1]);
                break;
            case "/update":
                OnlineList.modifyPublicKey(splitMsg[1], RSAUtils.stringToPublicKey(splitMsg[2]));
                break;
            default:
                break;
        }
    }

}
