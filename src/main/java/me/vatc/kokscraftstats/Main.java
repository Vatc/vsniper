package me.vatc.kokscraftstats;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Main.primaryStage = primaryStage;
        showFirstScene();
        primaryStage.show();
    }

    public static void showFirstScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/me/vatc/kokscraftstats/firstScene.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        FirstSceneController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);
    }

    public static void showSecondScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/me/vatc/kokscraftstats/secondScene.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        SecondSceneController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
