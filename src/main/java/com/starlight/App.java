package com.starlight;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.util.Duration;
import com.starlight.util.FXMLVerificator;
import com.starlight.api.UserApiServer;

import java.io.IOException;

public class App extends Application {


    private static Scene scene;
    private UserApiServer apiServer;
    private Thread apiThread;
    
    @Override
    public void start(Stage stage) throws IOException {
        apiServer = new UserApiServer(8000);
        apiThread = new Thread(apiServer::start);
        apiThread.setDaemon(true);
        apiThread.start();

        FXMLVerificator.verifyAll();
        scene = new Scene(loadFXML("splashScreen"), 1280, 720);

        scene = new Scene(loadFXML("splashScreen"), 1280, 720);
        stage.setScene(scene);
        stage.setTitle("TabemonPal by Starlight Inc.");
        stage.setResizable(true);
        stage.show();

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> showLogin());
        delay.play();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    public static void showLogin() {
        try {
            scene.setRoot(loadFXML("login"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void loadMainWithSplash() {
        try {
            scene.setRoot(loadFXML("splashScreen"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Task<Parent> task = new Task<>() {
            @Override
            protected Parent call() throws Exception {
                return loadFXML("main");
            }
        };
        task.setOnSucceeded(e -> scene.setRoot(task.getValue()));
        new Thread(task).start();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("view/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    @Override
    public void stop() throws Exception {
        if (apiServer != null) {
            apiServer.stop();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }

}