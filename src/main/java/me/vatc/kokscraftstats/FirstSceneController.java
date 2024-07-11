package me.vatc.kokscraftstats;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.text.SimpleDateFormat;
import java.util.Date;

import static me.vatc.kokscraftstats.utils.LogsUtil.extractUsername;
import static me.vatc.kokscraftstats.utils.LogsUtil.split;

public class FirstSceneController {
    @FXML
    private TableView<LogEntry> GlobalTable;
    @FXML
    private TableColumn<LogEntry, String> playerName;
    @FXML
    private TableColumn<LogEntry, String> playerRank;
    @FXML
    private TableColumn<LogEntry, String> playerTags;
    @FXML
    private Button settingsButton;

    private static final String LOG_DIRECTORY = System.getProperty("user.home") + "\\.lunarclient\\logs\\game";
    private static final String SPECIAL_TEXT = "dolacza do gry";
    private static final String SETTING_USER_TEXT = "Setting user:";
    private static final String ASSETS_MESSAGE = "[LC] Assets";
    private static final String LEAVING_MESSAGE = " wychodzi z serwera!";

    private long lastKnownPosition = 0;
    private File currentLogFile;
    private Stage primaryStage;

    private ObservableList<LogEntry> logData = FXCollections.observableArrayList();
    private String latestSettingUser = ""; // Variable to store the latest setting user

    @FXML
    public void initialize() {
        playerName.setCellValueFactory(new PropertyValueFactory<>("playerName"));
        playerRank.setCellValueFactory(new PropertyValueFactory<>("playerRank"));
        playerTags.setCellValueFactory(new PropertyValueFactory<>("playerTags"));

        GlobalTable.setItems(logData);

        Label placeholder = new Label("Wybierz arenę, aby zapisać graczy.");
        GlobalTable.setPlaceholder(placeholder);

        findLatestLogFile();
        watchLogFile();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private void findLatestLogFile() {
        File dir = new File(LOG_DIRECTORY);
        File[] logFiles = dir.listFiles((dir1, name) -> name.toLowerCase().endsWith(".log"));

        if (logFiles != null && logFiles.length > 0) {
            File newestLogFile = null;
            long lastModifiedTime = Long.MIN_VALUE;

            for (File file : logFiles) {
                if (file.lastModified() > lastModifiedTime) {
                    newestLogFile = file;
                    lastModifiedTime = file.lastModified();
                }
            }

            currentLogFile = newestLogFile;
            lastKnownPosition = currentLogFile.length(); // Set position to end of file
        } else {
            System.out.println("No .log files found in directory: " + LOG_DIRECTORY);
        }
    }

    private void watchLogFile() {
        if (currentLogFile == null) {
            System.out.println("No log file to monitor.");
            return;
        }

        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            currentLogFile.toPath().getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            Thread watchThread = new Thread(() -> {
                try {
                    WatchKey key;
                    while ((key = watchService.take()) != null) {
                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();
                            if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                                Path changed = (Path) event.context();
                                if (changed != null && changed.toString().equals(currentLogFile.getName())) {
                                    readNewLinesFromFile(currentLogFile);
                                }
                            }
                        }
                        key.reset();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            watchThread.setDaemon(true);
            watchThread.start();

            readNewLinesFromFile(currentLogFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readNewLinesFromFile(File file) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            // Search the whole file for SETTING_USER_TEXT only once
            long fileLength = raf.length();
            String line;

            raf.seek(lastKnownPosition); // Set pointer to last known position for SPECIAL_TEXT

            while ((line = raf.readLine()) != null) {
                lastKnownPosition = raf.getFilePointer();

                if (line.contains(ASSETS_MESSAGE)) {
                    clearTable();
                } else if (line.contains(SPECIAL_TEXT)) {
                    handleSpecialTextLine(line);
                } else if (line.contains(SETTING_USER_TEXT)) {
                    latestSettingUser = extractUsernameFromSettingUser(line); // Update latest setting user
                } else if (line.contains(LEAVING_MESSAGE)) {
                    handleLeavingMessage(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleSpecialTextLine(String line) {
        String[] result = split(extractUsername(line));
        String rank = (result.length > 1) ? result[0] : "";
        String name = (result.length > 0) ? result[result.length - 1] : "";

        if (name.equals("Vatc") || name.equals("Puszak")) {
            addOrUpdateLogEntry(name, rank, "Sigma skilled player");
        } else {
            addOrUpdateLogEntry(name, rank, "");
        }
    }

    private void handleLeavingMessage(String line) {
        String user = extractUsernameFromLeavingServer(line);
        if (user != null) {
            removeEntry(user);
        }
    }

    private void addOrUpdateLogEntry(String name, String rank, String tags) {
        Platform.runLater(() -> {
            boolean found = false;
            for (LogEntry entry : logData) {
                if (entry.getPlayerName().equals(name)) {
                    entry.setPlayerRank(rank);
                    entry.setPlayerTags(tags);
                    found = true;
                    break;
                }
            }
            if (!found) {
                logData.add(new LogEntry(name, rank, tags));
            }
        });
    }

    private void removeEntry(String username) {
        Platform.runLater(() -> {
            LogEntry entryToRemove = null;
            for (LogEntry entry : logData) {
                if (entry.getPlayerName().equals(username)) {
                    entryToRemove = entry;
                    break;
                }
            }
            if (entryToRemove != null) {
                logData.remove(entryToRemove);
            }
        });
    }

    private void clearTable() {
        Platform.runLater(() -> {
            logData.clear();
        });
    }

    @FXML
    private void clearTableFXML() {
        Platform.runLater(() -> {
            logData.clear();
        });
    }

    @FXML
    private void switchToSecondScene() throws Exception {
        Main.showSecondScene();
    }

    public static class LogEntry {
        private final String playerName;
        private String playerRank;
        private String playerTags;

        public LogEntry(String playerName, String playerRank, String playerTags) {
            this.playerName = playerName;
            this.playerRank = playerRank;
            this.playerTags = playerTags;
        }

        public String getPlayerName() {
            return playerName;
        }

        public String getPlayerRank() {
            return playerRank;
        }

        public void setPlayerRank(String playerRank) {
            this.playerRank = playerRank;
        }

        public String getPlayerTags() {
            return playerTags;
        }

        public void setPlayerTags(String playerTags) {
            this.playerTags = playerTags;
        }
    }

    private String extractUsernameFromSettingUser(String line) {
        int startIndex = line.indexOf(SETTING_USER_TEXT) + SETTING_USER_TEXT.length();
        return line.substring(startIndex).trim();
    }

    private String extractUsernameFromLeavingServer(String line) {
        int startIndex = line.lastIndexOf("[CHAT]") + "[CHAT]".length();
        int endIndex = line.indexOf(" wychodzi z serwera!");
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            return line.substring(startIndex, endIndex).trim();
        }
        return null;
    }

    private String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        return formatter.format(date);
    }
}