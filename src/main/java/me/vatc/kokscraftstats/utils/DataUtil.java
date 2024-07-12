package me.vatc.kokscraftstats.utils;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class DataUtil {

    private static final String CONFIG_FILE_NAME = "config.yml";

    private static DataUtil instance;

    private Yaml yaml;
    private Map<String, Object> configData;

    private DataUtil() {
        yaml = new Yaml();
        configData = new HashMap<>();
        loadConfig();
    }

    public static synchronized DataUtil getInstance() {
        if (instance == null) {
            instance = new DataUtil();
        }
        return instance;
    }

    private void loadConfig() {
        Path configPath = getConfigPath();
        if (Files.exists(configPath)) {
            try (InputStream inputStream = new FileInputStream(configPath.toFile())) {
                configData = yaml.load(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Config file does not exist. Creating a new one.");
        }
    }

    public void saveConfig() {
        Path configPath = getConfigPath();
        try (OutputStream outputStream = new FileOutputStream(configPath.toFile())) {
            yaml.dump(configData, new FileWriter(configPath.toFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object getConfigValue(String key) {
        return configData.get(key);
    }

    public void setConfigValue(String key, Object value) {
        configData.put(key, value);
        saveConfig();
    }

    public String getString(String key) {
        return (String) configData.get(key);
    }

    public void setString(String key, String value) {
        configData.put(key, value);
        saveConfig();
    }

    public Double getDouble(String key) {
        return (Double) configData.get(key);
    }

    public void setDouble(String key, Double value) {
        configData.put(key, value);
        saveConfig();
    }

    // Dodaj inne metody get/set w zależności od typów danych potrzebnych w Twojej aplikacji

    private Path getConfigPath() {
        // Get the user's AppData directory
        String appDataDir = System.getenv("APPDATA");
        if (appDataDir == null) {
            throw new RuntimeException("APPDATA environment variable not found.");
        }

        // Append your application folder and config file name
        Path configDir = Paths.get(appDataDir, "Kokslify");
        if (!Files.exists(configDir)) {
            try {
                Files.createDirectories(configDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return configDir.resolve(CONFIG_FILE_NAME);
    }
}

