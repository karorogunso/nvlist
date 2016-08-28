package nl.weeaboo.vn;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class InitConfig {

    private static final Logger LOG = LoggerFactory.getLogger(InitConfig.class);

    private InitConfig() {
    }

    public static void init() {
        configLogging();
    }

    private static void configLogging() {
        System.setProperty("sun.io.serialization.extendedDebugInfo", "true");

        File saveFolder = new File("save");
        if (!saveFolder.isDirectory() && !saveFolder.mkdirs()) {
            LOG.warn("Unable to create log folder");
        } else {
            InputStream in = Launcher.class.getResourceAsStream("logging.properties");
            if (in == null) {
                LOG.warn("Unable to read logging config");
            } else {
                try {
                    try {
                        LogManager.getLogManager().readConfiguration(in);
                    } finally {
                        in.close();
                    }
                } catch (IOException e) {
                    LOG.warn("Unable to read logging config", e);
                }
            }
        }

        setLogLevel("nl.weeaboo.vn", Level.FINE);
        setLogLevel("nl.weeaboo.gdx", Level.FINE);
    }

    private static void setLogLevel(String loggerName, Level level) {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(loggerName);
        logger.setLevel(level);
    }

}
