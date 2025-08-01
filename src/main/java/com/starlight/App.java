package com.starlight;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import com.starlight.util.FXMLVerificator;
import com.starlight.util.FileSystemManager;
import com.starlight.api.UserApiServer;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;
/**
 * Main JavaFX application entry point. This class starts the embedded
 * {@link UserApiServer} and loads the initial FXML views.
 */
public class App extends Application {

    private static final Logger logger = Logger.getLogger(App.class.getName());

    /** Main application scene used to swap views. */
    private static Scene scene;

    /**
     * Resizes the application window and centers it on the screen.
     *
     * @param width  The new width of the window.
     * @param height The new height of the window.
     */
    public static void resizeWindow(double width, double height) {
        Stage stage = (Stage) scene.getWindow();
        if (stage != null) {
            stage.setWidth(width);
            stage.setHeight(height);
            stage.centerOnScreen();
        }
    }

    /**
     * Maximizes the application window to fill the screen.
     */
    public static void maximizeStage() {
        Stage stage = (Stage) scene.getWindow();
        if (stage != null) {
            stage.setMaximized(true);
        }
    }

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
        scene = new Scene(loadFXML("splashScreen"));
        stage.setScene(scene);
        resizeWindow(1280, 720);

        try {
            Image icon = new Image(getClass().getResourceAsStream("/com/starlight/images/AppLogo.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not load application icon: " + e.getMessage(), e);
        }
        
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

        
        // Maximize the stage based on the loaded FXML
        if ("main".equals(fxml)) {
            maximizeStage();
        } else if ("Authorization".equals(fxml)) {
            resizeWindow(1280, 720);
        }
    }

    /**
     * Loads and displays the login screen.
     */
    public static void showLogin() {
        try {
            setRoot("Authorization");
        } catch (IOException e) {
            e.printStackTrace();
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
            logger.log(Level.SEVERE, "Failed to load splash screen: " + ex.getMessage(), ex);
            return;
        }

        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(e -> {
            try {
                setRoot("main");
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Failed to load main view: " + ex.getMessage(), ex);
                
                // Fallback - try to load main directly if there was an error
                try {
                    logger.info("Attempting direct load of main view");
                    setRoot("main");
                } catch (IOException fallbackEx) {
                    logger.log(Level.SEVERE, "Fallback failed too: " + fallbackEx.getMessage(), fallbackEx);
                }
            }
        });
        delay.play();
    }

    /**
     * Utility method to load an FXML file from the view directory.
     *
     * @param fxml name of the FXML file without extension
     * @return the loaded {@link Parent}
     */
    private static Parent loadFXML(String fxml) throws IOException {
        String resourcePath = "view/" + fxml + ".fxml";
        
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(resourcePath));
        if (fxmlLoader.getLocation() == null) {
            logger.warning("FXML resource not found: " + resourcePath);
            // Try with leading slash
            resourcePath = "/com/starlight/view/" + fxml + ".fxml";
            logger.info("Trying alternative path: " + resourcePath);
            fxmlLoader = new FXMLLoader(App.class.getClassLoader().getResource(resourcePath));
            
            if (fxmlLoader.getLocation() == null) {
                throw new IOException("Could not find FXML resource: " + fxml + ".fxml");
            }
        }
        
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
        FileSystemManager.initializeAppDataDirectory();
        launch();

        /*Login information 
         * 
         * Administrator:
         * Username: admin
         * Password: admin123
         * 
         * User:
         * Username: user
         * Password: user
         *
        */
    }

}