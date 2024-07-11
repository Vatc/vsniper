package me.vatc.kokscraftstats;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import static me.vatc.kokscraftstats.utils.LogsUtil.*;

public class FirstSceneController {
    @FXML
    private TableView<LogEntry> GlobalTable;
    @FXML
    private TableColumn<LogEntry, String> playerName;
    @FXML
    private TableColumn<LogEntry, String> playerRank; // Nowa kolumna
    @FXML
    private TableColumn<LogEntry, String> playerTags; // Nowa kolumna
    @FXML
    private Button settingsButton;

    private static final String LOG_DIRECTORY = System.getProperty("user.home") + "\\.lunarclient\\logs\\game";
    private static final String SPECIAL_TEXT = "dolacza do gry";

    private long lastKnownPosition = 0;
    private File currentLogFile;
    private Stage primaryStage;

    private ObservableList<LogEntry> logData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        playerName.setCellValueFactory(new PropertyValueFactory<>("playerName"));
        playerRank.setCellValueFactory(new PropertyValueFactory<>("playerRank")); // Wiązanie kolumny logTime z polem "time" w LogEntry
        playerTags.setCellValueFactory(new PropertyValueFactory<>("playerTags")); // Wiązanie kolumny logInfo z polem "info" w LogEntry

        GlobalTable.setItems(logData);
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
            raf.seek(lastKnownPosition);

            String line;
            while ((line = raf.readLine()) != null) {
                lastKnownPosition = raf.getFilePointer();

                if (line.contains(SPECIAL_TEXT)) {
                    String finalLine = line;
                    String[] result = split(extractUsername(finalLine));
                    System.out.println(extractUsername(finalLine));
                    String rank;
                    String name;
                    if (result.length <= 1) {
                        rank = "";
                    } else {
                        rank = result[0];
                    }

                    if (result.length == 1) {
                        name = result[0];
                    } else {
                        name = result[1];
                    }


                    Platform.runLater(() -> {
                        logData.add(new LogEntry(name, rank, "-"));
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToSecondScene() throws Exception {
        Main.showSecondScene();
    }

    public static class LogEntry {
        private final String playerName;
        private final String playerRank;
        private final String playerTags;

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

        public String getPlayerTags() {
            return playerTags;
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        return formatter.format(date);
    }

    @FXML
    private void clearTable() {
        logData.clear();
    }
}