package chat.Shared;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

public class PacketManager {
    private static final Logger logger = LogManager.getLogger(PacketManager.class);
    static {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);
    }

    public static String createMsgPacket(String src, String dest, String msg, String AESkey) {
        String packet = new JSONStringer()
            .object()
                .key("src")
                .value(src)
            
                .key("dest")
                .value(dest)
            
                .key("msg")
                .value(msg)
            
                .key("key")
                .value(AESkey)
            .endObject()
            .toString();

        logger.debug("Message packet created: ", packet);
        return packet;
    }

    public static boolean checkPacketFormat(String packet) {
        try {
            JSONObject obj = (JSONObject) new JSONTokener(packet).nextValue();
            obj.get("src");
            obj.get("dest");
            obj.get("msg");
            obj.get("key");
            logger.info("Packet format is valid.");
        } catch(Exception e) {
            logger.error("Invalid packet format: ", packet, e);
            return false;
        }
        return true;
    }

    public static String getPacketSrc(String packet) {
        if(checkPacketFormat(packet)){
            JSONObject obj = (JSONObject) new JSONTokener(packet).nextValue();
            return obj.get("src").toString();
        }
        return null;
    }

    public static String getPacketDest(String packet) {
        if(checkPacketFormat(packet)){
            JSONObject obj = (JSONObject) new JSONTokener(packet).nextValue();
            return obj.get("dest").toString();
        }
        return null;
    }

    public static String getPacketMsg(String packet) {
        if(checkPacketFormat(packet)){
            JSONObject obj = (JSONObject) new JSONTokener(packet).nextValue();
            return obj.get("msg").toString();
        }
        return null;
    }

    public static String getPacketKey(String packet) {
        if(checkPacketFormat(packet)){
            JSONObject obj = (JSONObject) new JSONTokener(packet).nextValue();
            return obj.get("key").toString();
        }
        return null;
    }

    //public static void main(String[] args) {
    //    System.out.println(getPacketSrc(createMsgPacket("elir", "toni", "messaggio", "343fsdf")));
    //    System.out.println(getPacketDest(createMsgPacket("elir", "toni", "messaggio", "343fsdf")));
    //    System.out.println(getPacketMsg(createMsgPacket("elir", "toni", "messaggio", "343fsdf")));
    //    System.out.println(getPacketKey(createMsgPacket("elir", "toni", "messaggio", "343fsdf")));
    //}
}
