package me.vatc.kokscraftstats;

import javafx.fxml.FXML;
import javafx.stage.Stage;

public class SecondSceneController {

    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void switchToFirstScene() throws Exception {
        Main.showFirstScene();
    }
}
