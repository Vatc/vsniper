package me.vatc.kokscraftstats;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.util.Callback;
import me.vatc.kokscraftstats.utils.DataUtil;
import me.vatc.kokscraftstats.utils.TagUtil;

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
    private double xOffset = 0;
    private double yOffset = 0;

    private long lastProcessedTime = 0;

    @FXML
    private VBox rootPane;

    @FXML
    private Button closeButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button minimizeButton;


    private TagUtil tagUtil;

    public FirstSceneController() {
        tagUtil = tagUtil.getInstance();
    }

    @FXML

    private ObservableList<LogEntry> logData = FXCollections.observableArrayList();
    private String latestSettingUser = ""; // Variable to store the latest setting user

    @FXML
    public void initialize() {

        Image iconImage = new Image(getClass().getResourceAsStream("/me/vatc/kokscraftstats/icons/close.png"));
        ImageView iconView = new ImageView(iconImage);
        iconView.setFitWidth(28);
        iconView.setFitHeight(28);
        closeButton.setGraphic(iconView);

        Image iconImage2 = new Image(getClass().getResourceAsStream("/me/vatc/kokscraftstats/icons/minimize.png"));
        ImageView iconView2 = new ImageView(iconImage2);
        iconView2.setFitWidth(28);
        iconView2.setFitHeight(28);
        minimizeButton.setGraphic(iconView2);

        Image iconImage3 = new Image(getClass().getResourceAsStream("/me/vatc/kokscraftstats/icons/settings.png"));
        ImageView iconView3 = new ImageView(iconImage3);
        iconView3.setFitWidth(28);
        iconView3.setFitHeight(28);
        settingsButton.setGraphic(iconView3);

        Image iconImage4 = new Image(getClass().getResourceAsStream("/me/vatc/kokscraftstats/icons/refresh.png"));
        ImageView iconView4 = new ImageView(iconImage4);
        iconView4.setFitWidth(28);
        iconView4.setFitHeight(28);
        refreshButton.setGraphic(iconView4);

        Image iconImage5 = new Image(getClass().getResourceAsStream("/me/vatc/kokscraftstats/icons/delete.png"));
        ImageView iconView5 = new ImageView(iconImage5);
        iconView5.setFitWidth(28);
        iconView5.setFitHeight(28);
        deleteButton.setGraphic(iconView5);


        playerName.setCellValueFactory(new PropertyValueFactory<>("playerName"));
        playerRank.setCellValueFactory(new PropertyValueFactory<>("playerRank"));
        playerTags.setCellValueFactory(new PropertyValueFactory<>("playerTags"));

        GlobalTable.setItems(logData);

        Label placeholder = new Label("Wybierz arenę, aby zapisać graczy.");
        GlobalTable.setPlaceholder(placeholder);

        // Set cell factory to add custom styling for each column
        playerName.setCellFactory(createCellFactory());
        playerRank.setCellFactory(createCellFactory());
        playerTags.setCellFactory(createCellFactory());

        findLatestLogFile();
        watchLogFile();

        rootPane.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        rootPane.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });
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
            // Przeszukaj plik tylko od ostatnio znanej pozycji, ale po czasie lastProcessedTime
            raf.seek(lastKnownPosition);
            String line;
            while ((line = raf.readLine()) != null) {
                lastKnownPosition = raf.getFilePointer();
                String currentTime = getCurrentTime(); // Pobierz bieżący czas
                if (line.contains(ASSETS_MESSAGE)) {
                    clearTable();
                } else if (line.contains(SPECIAL_TEXT)) {
                    handleSpecialTextLine(line);
                    Platform.runLater(() -> {
                        primaryStage.setIconified(false);
                    });
                } else if (line.contains(SETTING_USER_TEXT)) {
                    latestSettingUser = extractUsernameFromSettingUser(line); // Update latest setting user
                } else if (line.contains(LEAVING_MESSAGE)) {
                    handleLeavingMessage(line);
                } else if (line.contains("[CHAT] Start!") && parseTime(line) > lastProcessedTime) {
                    Platform.runLater(() -> {
                        primaryStage.setIconified(true);
                    });
                    lastProcessedTime = parseTime(line); // Ustaw nowy czas ostatnio przetworzonej linii
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private long parseTime(String line) {
        // Funkcja do parsowania czasu z linii, która zawiera "[CHAT] Start!"
        // Przykładowa implementacja, zależy od formatu czasu w logach
        return System.currentTimeMillis(); // Zwróć bieżący czas w milisekundach jako przykład
    }

    private void handleSpecialTextLine(String line) {
        String[] result = split(extractUsername(line));
        String rank = (result.length > 1) ? result[0] : "";
        String name = (result.length > 0) ? result[result.length - 1] : "";

        System.out.println(latestSettingUser);
        if (name.equals(latestSettingUser)) {
            if (tagUtil.containsNick(name)) {
                addOrUpdateLogEntry(name, "TY" + rank, tagUtil.getTag(name), tagUtil.getColor(name));
            } else {
                addOrUpdateLogEntry(name, "TY" + rank, "", "");
            }
        } else {
            if (tagUtil.containsNick(name)) {
                addOrUpdateLogEntry(name, rank, tagUtil.getTag(name), tagUtil.getColor(name));
            } else {
                addOrUpdateLogEntry(name, rank, "", "");
            }
        }
    }

    private void handleLeavingMessage(String line) {
        String user = extractUsernameFromLeavingServer(line);
        if (user != null) {
            removeEntry(user);
        }
    }

    private void addOrUpdateLogEntry(String name, String rank, String tags, String backgroundColor) {
        Platform.runLater(() -> {
            boolean found = false;
            for (LogEntry entry : logData) {
                if (entry.getPlayerName().equals(name)) {
                    entry.setPlayerRank(rank);
                    entry.setPlayerTags(tags);
                    entry.setBackgroundColor(backgroundColor);
                    found = true;
                    break;
                }
            }
            if (!found) {
                logData.add(new LogEntry(name, rank, tags, backgroundColor));
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
        private String backgroundColor;

        public LogEntry(String playerName, String playerRank, String playerTags, String backgroundColor) {
            this.playerName = playerName;
            this.playerRank = playerRank;
            this.playerTags = playerTags;
            this.backgroundColor = backgroundColor;
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

        public String getBackgroundColor() {
            return backgroundColor;
        }

        public void setBackgroundColor(String backgroundColor) {
            this.backgroundColor = backgroundColor;
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

    @FXML
    private void minimizeWindow() {
        primaryStage.setIconified(true);
    }

    @FXML
    private void closeWindow() {
        primaryStage.close();
    }

    private Callback<TableColumn<LogEntry, String>, TableCell<LogEntry, String>> createCellFactory() {
        return column -> new TableCell<LogEntry, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle(""); // Upewnij się, że czyszczesz styl, jeśli komórka jest pusta
                } else {
                    setText(item);
                    // Get corresponding LogEntry
                    LogEntry entry = getTableView().getItems().get(getIndex());
                    // Set background color style
                    if (entry.getBackgroundColor() != null && !entry.getBackgroundColor().isEmpty()) {
                        setStyle("-fx-background-color: " + entry.getBackgroundColor() + ";");
                    } else {
                        setStyle(""); // Upewnij się, że czyszczesz styl, jeśli kolor tła nie jest ustawiony
                    }
                }
            }
        };
    }
}
