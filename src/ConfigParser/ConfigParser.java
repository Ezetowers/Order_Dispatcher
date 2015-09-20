package configParser;

import java.lang.IllegalArgumentException;
import java.io.FileReader;
import java.io.IOException;

// External imports
import org.ini4j.Ini;

import logger.Logger;
import logger.LogLevel;


public class ConfigParser {
    private ConfigParser() {}

    public void init(String filePath) {
        // TODO: Receive the config file from an argument
        String configFileName = filePath;
        config_ = new Ini();

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
        if (configParser_ == null) {
            configParser_ = new ConfigParser();
        }

        return configParser_;
    }

    public String get(String section, String key, String defaultValue) {
        String value = config_.get(section, key);
        if (value != null) {
            return value;
        }

        Logger.getInstance().log(LogLevel.INFO, 
            "[CONFIGPARSER] Key (" + section + ", " + key 
            + ") was not found. Using default value: " + defaultValue);
        return defaultValue;
    }

    public String get(String section, String key) {
        String value = config_.get(section, key);;
        if (value != null) {
            return value;
        }

        String msg = "Value doesn't exists in Config File. Section: "
            + section + " - Key: " + key;
        throw new IllegalArgumentException(msg);
    }

    private static ConfigParser configParser_ = null;
    private Ini config_;
}