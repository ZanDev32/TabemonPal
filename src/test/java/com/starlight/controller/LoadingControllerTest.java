package com.starlight.controller;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Unit tests for {@link LoadingController}.
 */
public class LoadingControllerTest extends ApplicationTest {
    
    private LoadingController controller;
    
    @BeforeAll
    static void setUpClass() {
        // Ensure JavaFX is initialized for headless testing
        System.setProperty("java.awt.headless", "true");
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("glass.platform", "Monocle");
        System.setProperty("monocle.platform", "Headless");
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        // Create a minimal test scene
        VBox root = new VBox();
        Scene scene = new Scene(root, 400, 300);
        stage.setScene(scene);
        
        // Create the controller
        controller = new LoadingController();
        
        stage.show();
    }
    
    @Test
    void testControllerInstantiation() {
        assertNotNull(controller);
    }
    
    @Test
    void testControllerCanBeCreated() {
        // Test that multiple controller instances can be created
        assertDoesNotThrow(() -> {
            LoadingController controller1 = new LoadingController();
            LoadingController controller2 = new LoadingController();
            
            assertNotNull(controller1);
            assertNotNull(controller2);
            assertNotSame(controller1, controller2);
        });
    }
    
    @Test
    void testInitializeMethodExists() {
        // Test that the initialize method exists using reflection
        assertDoesNotThrow(() -> {
            var initializeMethod = LoadingController.class.getDeclaredMethod("initialize");
            assertNotNull(initializeMethod);
            
            // Check that it's annotated with @FXML
            assertTrue(initializeMethod.isAnnotationPresent(javafx.fxml.FXML.class));
        });
    }
    
    @Test
    void testInitializeMethod() {
        // Test that the initialize method can be called
        assertDoesNotThrow(() -> {
            // Use reflection to call the private initialize method
            var initializeMethod = LoadingController.class.getDeclaredMethod("initialize");
            initializeMethod.setAccessible(true);
            initializeMethod.invoke(controller);
        });
    }
    
    @Test
    void testControllerStructure() {
        // Test that the controller has the expected structure
        Class<?> controllerClass = LoadingController.class;
        
        // Should be in the correct package
        assertEquals("com.starlight.controller", controllerClass.getPackage().getName());
        
        // Should be a public class
        assertTrue(java.lang.reflect.Modifier.isPublic(controllerClass.getModifiers()));
    }
    
    @Test
    void testMultipleInitializeCalls() {
        // Test that initialize can be called multiple times without issues
        assertDoesNotThrow(() -> {
            var initializeMethod = LoadingController.class.getDeclaredMethod("initialize");
            initializeMethod.setAccessible(true);
            
            // Call initialize multiple times
            initializeMethod.invoke(controller);
            initializeMethod.invoke(controller);
            initializeMethod.invoke(controller);
        });
    }
    
    @Test
    void testControllerHasNoPublicFields() {
        // Ensure the controller doesn't expose any public fields (good encapsulation)
        var fields = LoadingController.class.getFields();
        assertEquals(0, fields.length, "Controller should not have public fields");
    }
}
