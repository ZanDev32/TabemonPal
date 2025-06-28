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

/**
 * Main JavaFX application entry point. This class starts the embedded
 * {@link UserApiServer} and loads the initial FXML views.
 */
public class App extends Application {


    /** Main application scene used to swap views. */
    private static Scene scene;
    /** Embedded HTTP server providing user API endpoints. */
    private UserApiServer apiServer;
    /** Thread running the API server. */
    private Thread apiThread;
    
    /**
     * Starts the JavaFX application and shows the splash screen. The user API
     * server is started in a background thread.
     */
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

    /**
     * Replaces the current scene root with the given FXML view.
     *
     * @param fxml name of the FXML file without extension
     */
    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    /**
     * Loads and displays the login screen.
     */
    public static void showLogin() {
        try {
            scene.setRoot(loadFXML("login"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Shows the splash screen while the main view is loaded in the
     * background.
     */
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

    /**
     * Utility method to load an FXML file from the view directory.
     *
     * @param fxml name of the FXML file without extension
     * @return the loaded {@link Parent}
     */
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("view/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    /**
     * Stops the API server when the JavaFX application terminates.
     */
    @Override
    public void stop() throws Exception {
        if (apiServer != null) {
            apiServer.stop();
        }
        super.stop();
    }

    /**
     * Launches the JavaFX application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch();
    }

}