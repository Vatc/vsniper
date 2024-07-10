package me.vatc.kokscraftstats;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.*;

public class FirstSceneController {
    @FXML
    private ScrollPane scrollGlobal;
    @FXML
    private TextArea textAreaLogs;
    @FXML
    private Button settingsButton;

    private static final String LOG_DIRECTORY = "C:\\Users\\Pc\\.lunarclient\\logs\\game";
    private static final String SPECIAL_TEXT = "Vatc";

    private long lastKnownPosition = 0;
    private File currentLogFile;
    private Stage primaryStage;

    @FXML
    public void initialize() {
        textAreaLogs.setWrapText(true);
        textAreaLogs.setEditable(false);
        scrollGlobal.setContent(textAreaLogs);
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
                    Platform.runLater(() -> {
                        textAreaLogs.appendText("Znaleziono " + SPECIAL_TEXT + ": " + finalLine + "\n");
                        scrollGlobal.setVvalue(1.0);
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
}
