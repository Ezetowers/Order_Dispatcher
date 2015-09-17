package configparser;

import java.io.FileReader;
import java.io.IOException;

// External imports
import org.ini4j.Ini;

import logger.Logger;
import logger.LogLevel;


public class ConfigParser {
    private ConfigParser() {}

    public static void init() {
        // TODO: Receive the config file from an argument
        String configFileName = "configuration.ini";

        try {
            config_.load(new FileReader(configFileName));
        }
        catch(IOException e) {
            System.err.println("[CONFIGPARSER] Could not open config file.");
            System.err.println(e);
            System.exit(-1);
        } 
    }

    public static ConfigParser getInstance() {
        return configParser_;
    }

    public static String get(String section, String key, String defaultValue) {
        String value = config_.get(section, key);
        if (value != null) {
            return value;
        }

        Logger.log(LogLevel.INFO, "[CONFIGPARSER] Key (" + section + ", " + key 
            + ") was not found. Using default value: " + defaultValue);
        return defaultValue;
    }

    public static String get(String section, String key) {
        return config_.get(section, key);
    }

    private static Ini config_ = new Ini();
    private static ConfigParser configParser_ = new ConfigParser();
}