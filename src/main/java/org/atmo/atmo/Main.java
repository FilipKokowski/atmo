package org.atmo.atmo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL fxmlUrl = getClass().getResource("weather-view.fxml");
        if (fxmlUrl == null) {
            System.err.println("Cannot find weather-view.fxml");
            return;
        }
        Parent root = FXMLLoader.load(fxmlUrl);

        primaryStage.setTitle("Atmo");

        try {
            Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icon.png")));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Nie udało się załadować ikony aplikacji.");
        }

        Scene scene = new Scene(root, 450, 735);

        URL cssUrl = getClass().getResource("style.css");
        if (cssUrl != null)
            scene.getStylesheets().add(cssUrl.toExternalForm());


        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}