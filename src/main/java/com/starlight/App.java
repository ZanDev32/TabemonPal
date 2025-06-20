package com.starlight;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.starlight.util.FXMLVerificator;

import java.io.IOException;

public class App extends Application {

    
    private static Scene scene;
    
    @Override
    public void start(Stage stage) throws IOException {
        FXMLVerificator.verifyAll();

        scene = new Scene(loadFXML("main"), 1280, 720);
        stage.setScene(scene);
        stage.setTitle("TabemonPal by Starlight Inc.");
        stage.setResizable(false);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("view/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}