package me.vatc.kokscraftstats;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import me.vatc.kokscraftstats.utils.DataUtil;
import me.vatc.kokscraftstats.utils.TagUtil;

import java.net.URL;
import java.util.ResourceBundle;

import static me.vatc.kokscraftstats.utils.OtherUtils.toHexString;

public class SecondSceneController implements Initializable {

    private Stage primaryStage;

    @FXML
    private ChoiceBox<String> minecraftClient;

    @FXML
    private Slider transparencySlider;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private ChoiceBox<String> minimalizeChoice;

    @FXML
    private ColorPicker themeColorPicker;
    private DataUtil dataUtil;

    private TagUtil tagUtil;

    public SecondSceneController() {
        dataUtil = DataUtil.getInstance();
        tagUtil = tagUtil.getInstance();
    }


    @FXML
    private TableView<PlayerSetting> settingsTable;
    @FXML
    private TableColumn<PlayerSetting, String> settingsPlayerName;
    @FXML
    private TableColumn<PlayerSetting, String> settingsPlayerTag;

    private ObservableList<PlayerSetting> playerSettings;

    @FXML
    private TextField playerNameInput;

    @FXML
    private TextField playerTagInput;

    @FXML
    private ColorPicker playerColorInput;

    @FXML
    private Button addTagButton;

    @FXML
    private VBox rootPane;

    @FXML
    private Button closeButton;

    @FXML
    private Button minimizeButton;
    @FXML
    private Button deleteFromTable;

    @FXML
    private Button backToMainButton;
    @Override
    public void initialize(URL location, ResourceBundle resources) {

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

        Image iconImage3 = new Image(getClass().getResourceAsStream("/me/vatc/kokscraftstats/icons/back.png"));
        ImageView iconView3 = new ImageView(iconImage3);
        iconView3.setFitWidth(28);
        iconView3.setFitHeight(28);
        backToMainButton.setGraphic(iconView3);

        Image iconImage4 = new Image(getClass().getResourceAsStream("/me/vatc/kokscraftstats/icons/delete.png"));
        ImageView iconView4 = new ImageView(iconImage4);
        iconView4.setFitWidth(28);
        iconView4.setFitHeight(28);
        deleteFromTable.setGraphic(iconView4);

        // Dodaj opcje do ChoiceBox
        ObservableList<String> options = FXCollections.observableArrayList(
                "Lunar Client", "Feather Client", "Default Minecraft"
        );
        minecraftClient.setItems(options);
        minecraftClient.setValue("Lunar Client");
        minecraftClient.setDisable(true);



        if (dataUtil.getDouble("setting.appTransparency") == null ) {
            dataUtil.setDouble("setting.appTransparency", 1.0);
        }
        Double selectedValueS = dataUtil.getDouble("setting.appTransparency");
        transparencySlider.setValue(selectedValueS);
        transparencySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (primaryStage != null) {
                primaryStage.setOpacity(newValue.doubleValue());
                dataUtil.setDouble("setting.appTransparency", newValue.doubleValue());
            }
        });

        rootPane.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        rootPane.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });

        ObservableList<String> optionsM = FXCollections.observableArrayList(
                "Zawsze", "Start areny", "Nigdy"
        );
        minimalizeChoice.setItems(optionsM);

        if (dataUtil.getString("setting.minimalizeChoice") == null ) {
            dataUtil.setString("setting.minimalizeChoice", "Nigdy");
        }

        String selectedValue = dataUtil.getString("setting.minimalizeChoice");
        minimalizeChoice.setValue(selectedValue);

        // Listener reagujący na zmiany w ChoiceBox
        minimalizeChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            dataUtil.setString("setting.minimalizeChoice", newValue); // Zapisz nową wartość do konfiguracji
        });

        if (dataUtil.getString("setting.themeColor") == null ) {
            dataUtil.setString("setting.themeColor", "#121212");
        }

        String storedColor = dataUtil.getString("setting.themeColor");
        if (storedColor != null && !storedColor.isEmpty()) {
            themeColorPicker.setValue(Color.web(storedColor));
        }

        // Dodaj listener reagujący na zmiany w ColorPickerze
        themeColorPicker.setOnAction(event -> {
            Color selectedColor = themeColorPicker.getValue();
            String hexColor = toHexString(selectedColor);
            dataUtil.setString("setting.themeColor", hexColor);
            handleChangeColor(hexColor);
        });

        settingsPlayerName.setCellValueFactory(new PropertyValueFactory<>("playerName"));
        settingsPlayerTag.setCellValueFactory(new PropertyValueFactory<>("playerTag"));

        // Ustawienie fabryk komórek dla kolumn
        settingsPlayerName.setCellFactory(column -> new TableCell<PlayerSetting, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    TableRow<PlayerSetting> row = getTableRow();
                    if (row != null) {
                        PlayerSetting setting = row.getItem();
                        if (setting != null) {
                            setStyle("-fx-background-color: " + setting.getBackgroundColor() + ";");
                        }
                    }
                }
            }
        });

        settingsPlayerTag.setCellFactory(column -> new TableCell<PlayerSetting, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    TableRow<PlayerSetting> row = getTableRow();
                    if (row != null) {
                        PlayerSetting setting = row.getItem();
                        if (setting != null) {
                            setStyle("-fx-background-color: " + setting.getBackgroundColor() + ";");
                        }
                    }
                }
            }
        });

        Label placeholder = new Label("Brak dodanych graczy specialnych.");
        settingsTable.setPlaceholder(placeholder);

        // Inicjalizacja listy danych
        playerSettings = FXCollections.observableArrayList();

        // Przypisanie danych do TableView
        settingsTable.setItems(playerSettings);

        // Załadowanie danych z TagUtil
        loadDataFromTagUtil();

        BooleanBinding areTextFieldsEmpty = Bindings.createBooleanBinding(() ->
                        playerNameInput.getText().trim().isEmpty() || playerTagInput.getText().trim().isEmpty(),
                playerNameInput.textProperty(),
                playerTagInput.textProperty()
        );

        addTagButton.disableProperty().bind(areTextFieldsEmpty);
        deleteFromTable.setDisable(true);

        // Set the button action to print the values
        addTagButton.setOnAction(event -> {
            String playerName = playerNameInput.getText();
            String playerTag = playerTagInput.getText();
            String playerColor = toHexString(playerColorInput.getValue());
            addPlayerSetting(playerName, playerTag, playerColor);

            clearInputs();
        });

        settingsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                deleteFromTable.setDisable(false); // Odblokowanie przycisku usuwania
            } else {
                deleteFromTable.setDisable(true); // Zablokowanie przycisku usuwania
            }
        });

    }

    @FXML
    private void handleDeleteButton() {
        PlayerSetting selectedSetting = settingsTable.getSelectionModel().getSelectedItem();
        if (selectedSetting != null) {
            playerSettings.remove(selectedSetting);
            tagUtil.removeTagData(selectedSetting.getPlayerName());
        }
    }

    private void clearInputs() {
        playerNameInput.clear();
        playerTagInput.clear();
        playerColorInput.setValue(Color.WHITE); // Przywróć domyślny kolor
    }
    private void loadDataFromTagUtil() {
        playerSettings.addAll(tagUtil.getAllEntries());
    }


    public void addPlayerSetting(String playerName, String playerTag, String backgroundColor) {
        tagUtil.setTagData(playerName, playerTag, backgroundColor);
        playerSettings.add(new PlayerSetting(playerName, playerTag, backgroundColor));
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void switchToFirstScene() throws Exception {
        Main.showFirstScene();
    }

    @FXML
    private void minimizeWindow() {
        primaryStage.setIconified(true);
    }

    @FXML
    private void closeWindow() {
        primaryStage.close();
    }

    @FXML
    private void handleChangeColor(String color) {
        if (primaryStage != null) {
            Scene scene = primaryStage.getScene();
            if (scene != null) {
                scene.setFill(Color.web(color)); // Ustaw kolor tła sceny na jasnoniebieski
            }
        }
    }

    public static class PlayerSetting {
        private String playerName;
        private String playerTag;
        private String backgroundColor;

        public PlayerSetting(String playerName, String playerTag, String backgroundColor) {
            this.playerName = playerName;
            this.playerTag = playerTag;
            this.backgroundColor = backgroundColor;
        }

        public String getPlayerName() {
            return playerName;
        }

        public void setPlayerName(String playerName) {
            this.playerName = playerName;
        }

        public String getPlayerTag() {
            return playerTag;
        }

        public void setPlayerTag(String playerTag) {
            this.playerTag = playerTag;
        }

        public String getBackgroundColor() {
            return backgroundColor;
        }

        public void setBackgroundColor(String backgroundColor) {
            this.backgroundColor = backgroundColor;
        }
    }
}
