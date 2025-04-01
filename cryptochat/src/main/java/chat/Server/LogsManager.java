package chat.Server;

import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.config.Configurator;

public class LogsManager {
    private static final Logger logger = LogManager.getLogger(LogsManager.class);
    
    public static void setUp(){
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);
    }

    public static void INFO(String msg) {
        logger.info(msg);
    }
}
