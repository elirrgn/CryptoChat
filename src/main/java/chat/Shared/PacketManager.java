package chat.Shared;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;


/**
 * Utility class to manage the packet format and data retrieve
 */
public class PacketManager {
    private static final Logger logger = LogManager.getLogger(PacketManager.class);
    static {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);
    }

    /**
     * Creates the packet message with the predefined format
     * 
     * @param src sender username
     * @param dest destination username or "all"
     * @param msg the encrypted message
     * @param AESkey the encrypted AESkey
     * @return the JSON as a String
     */
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


    /***
     * Checks if the packet has the correct JSON format
     * 
     * @param packet the packet to check
     * @return true if correct, false if not
     */
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

    /**
     * Gets the src field from the String JSON packet
     * 
     * @param packet the packet used
     * @return the src field or null if the packet format is not correct
     */
    public static String getPacketSrc(String packet) {
        if(checkPacketFormat(packet)){
            JSONObject obj = (JSONObject) new JSONTokener(packet).nextValue();
            return obj.get("src").toString();
        }
        return null;
    }

    /**
     * Gets the dest field from the String JSON packet
     * 
     * @param packet the packet used
     * @return the dest field or null if the packet format is not correct
     */
    public static String getPacketDest(String packet) {
        if(checkPacketFormat(packet)){
            JSONObject obj = (JSONObject) new JSONTokener(packet).nextValue();
            return obj.get("dest").toString();
        }
        return null;
    }

    /**
     * Gets the message field from the String JSON packet
     * 
     * @param packet the packet used
     * @return the message field or null if the packet format is not correct
     */
    public static String getPacketMsg(String packet) {
        if(checkPacketFormat(packet)){
            JSONObject obj = (JSONObject) new JSONTokener(packet).nextValue();
            return obj.get("msg").toString();
        }
        return null;
    }

    /**
     * Gets the aesKey field from the String JSON packet
     * 
     * @param packet the packet used
     * @return the src field or null if the packet format is not correct
     */
    public static String getPacketKey(String packet) {
        if(checkPacketFormat(packet)){
            JSONObject obj = (JSONObject) new JSONTokener(packet).nextValue();
            return obj.get("key").toString();
        }
        return null;
    }
}
