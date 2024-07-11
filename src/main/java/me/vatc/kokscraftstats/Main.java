package me.vatc.kokscraftstats;

import atlantafx.base.theme.Dracula;
import atlantafx.base.theme.Styles;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.FileInputStream;

public class Main extends Application {

    private static Stage primaryStage;

    @Override

    public void start(Stage primaryStage) throws Exception {
        Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());
        Main.primaryStage = primaryStage;
        showFirstScene();
        primaryStage.show();
    }

    public static void showFirstScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/me/vatc/kokscraftstats/firstScene.fxml"));
        Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());

        Image icon = new Image(new FileInputStream("src/main/resources/iconB.png"));
        primaryStage.getIcons().add(icon);
        primaryStage.setOpacity(0.9);

        primaryStage.setTitle("Kokslify");

        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        FirstSceneController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);
    }

    public static void showSecondScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/me/vatc/kokscraftstats/secondScene.fxml"));
        Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle("Kokslify");
        primaryStage.setScene(scene);

        SecondSceneController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
