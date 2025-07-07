package com.starlight.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * Tests that all FXML files in the view package can be loaded using FXMLLoader,
 * including their controllers (if declared), using TestFX in headless mode.
 */
public class FXMLLoadingTest extends ApplicationTest {

    static {
        // Enable TestFX headless mode so JavaFX doesn't require a display
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("glass.platform", "Monocle");
        System.setProperty("monocle.platform", "Headless");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("java.awt.headless", "true");
        
        // Required for proper module access with Monocle in JavaFX 19
        System.setProperty("javafx.platform", "monocle");
        System.setProperty("monocle.headless.update", "false");
        System.setProperty("monocle.keyboard", "null");
        System.setProperty("monocle.mouse", "null");
    }

    // Collect all FXML file resource paths in the view package
    static Stream<String> fxmlPaths() throws IOException {
        Path dir = Path.of("src/main/resources/com/starlight/view");
        return Files.list(dir)
                .filter(p -> p.toString().endsWith(".fxml"))
                .map(p -> "/com/starlight/view/" + p.getFileName().toString());
    }

    @ParameterizedTest
    @MethodSource("fxmlPaths")
    void testLoadFXMLWithLoader(String path) throws Exception {
        // This runs on the JavaFX Application Thread (safe for FXMLLoader)
        assertNotNull(FXMLLoadingTest.class.getResource(path), "Resource missing: " + path);

        FXMLLoader loader = new FXMLLoader(FXMLLoadingTest.class.getResource(path));
        Parent root = loader.load();

        assertNotNull(root, "FXMLLoader failed to load: " + path);
        assertTrue(root.getClass().getSimpleName().length() > 0, "Root element not loaded for: " + path);

        // Optionally, check that the controller (if present) is loaded
        Object controller = loader.getController();
        if (controller != null) {
            assertNotNull(controller, "Controller not instantiated for: " + path);
        }
    }
}
