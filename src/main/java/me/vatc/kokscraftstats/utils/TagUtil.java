package me.vatc.kokscraftstats.utils;

import me.vatc.kokscraftstats.SecondSceneController;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagUtil {

    private static final String CONFIG_FILE_NAME = "tagData.yml";
    private static TagUtil instance;
    private Yaml yaml;
    private Map<String, Map<String, String>> tagData;

    private TagUtil() {
        yaml = new Yaml();
        tagData = new HashMap<>();
        loadConfig();
    }

    public static TagUtil getInstance() {
        if (instance == null) {
            instance = new TagUtil();
        }
        return instance;
    }

    private void loadConfig() {
        Path configPath = getConfigPath();
        if (Files.exists(configPath)) {
            try (InputStream inputStream = new FileInputStream(configPath.toFile())) {
                tagData = yaml.load(inputStream);
                if (tagData == null) {
                    tagData = new HashMap<>();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassCastException e) {
                System.err.println("Error loading config file. Expected a Map<String, Map<String, String>> structure.");
                e.printStackTrace();
            }
        } else {
            System.out.println("Config file does not exist. Creating a new one.");
        }
    }

    public void saveConfig() {
        Path configPath = getConfigPath();
        try (OutputStream outputStream = new FileOutputStream(configPath.toFile())) {
            yaml.dump(tagData, new FileWriter(configPath.toFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setTagData(String nick, String tag, String color) {
        Map<String, String> data = new HashMap<>();
        data.put("tag", tag);
        data.put("color", color);
        tagData.put(nick, data);
        saveConfig();
    }

    public void removeTagData(String nick) {
        tagData.remove(nick);
        saveConfig();
    }

    public boolean containsNick(String nick) {
        return tagData.containsKey(nick);
    }

    public String getTag(String nick) {
        Map<String, String> data = tagData.get(nick);
        return data != null ? data.get("tag") : null;
    }

    public String getColor(String nick) {
        Map<String, String> data = tagData.get(nick);
        return data != null ? data.get("color") : null;
    }

    public List<SecondSceneController.PlayerSetting> getAllEntries() {
        List<SecondSceneController.PlayerSetting> entries = new ArrayList<>();
        try {
            for (Map.Entry<String, Map<String, String>> entry : tagData.entrySet()) {
                String nick = entry.getKey();
                String tag = entry.getValue().get("tag");
                String color = entry.getValue().get("color");
                entries.add(new SecondSceneController.PlayerSetting(nick, tag, color));
            }
        } catch (ClassCastException e) {
            System.err.println("Error iterating over tagData. Expected a Map<String, Map<String, String>> structure.");
            e.printStackTrace();
        }
        return entries;
    }

    private Path getConfigPath() {
        String appDataDir = System.getenv("APPDATA");
        if (appDataDir == null) {
            throw new RuntimeException("APPDATA environment variable not found.");
        }
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
