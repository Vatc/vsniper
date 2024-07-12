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
import javafx.stage.StageStyle;
import me.vatc.kokscraftstats.utils.DataUtil;

import java.io.FileInputStream;

public class Main extends Application {

    private static Stage primaryStage;

    private static DataUtil dataUtil;

    public Main() {
        dataUtil = DataUtil.getInstance();
    }

    @Override

    public void start(Stage primaryStage) throws Exception {
        Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());
        Main.primaryStage = primaryStage;

        primaryStage.initStyle(StageStyle.UNDECORATED);
        showFirstScene();
        primaryStage.show();
    }

    public static void showFirstScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/me/vatc/kokscraftstats/firstScene.fxml"));
        Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());

        Image icon = new Image(new FileInputStream("src/main/resources/iconB.png"));
        primaryStage.getIcons().add(icon);
        if (dataUtil.getDouble("setting.appTransparency") != null) {
            primaryStage.setOpacity(dataUtil.getDouble("setting.appTransparency"));
        }


        primaryStage.setAlwaysOnTop(true);

        primaryStage.setTitle("Kokslify");

        Parent root = loader.load();
        Scene scene = new Scene(root);
        if (dataUtil.getString("setting.themeColor") != null) {
            scene.setFill(Color.web(dataUtil.getString("setting.themeColor")));
        }
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
        if (dataUtil.getDouble("setting.appTransparency") != null) {
            primaryStage.setOpacity(dataUtil.getDouble("setting.appTransparency"));

        }
        primaryStage.setScene(scene);
        if (dataUtil.getString("setting.themeColor") != null) {
            scene.setFill(Color.web(dataUtil.getString("setting.themeColor")));
        }

        SecondSceneController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
