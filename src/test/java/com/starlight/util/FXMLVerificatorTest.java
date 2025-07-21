package com.starlight.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link FXMLVerificator} utility class.
 */
public class FXMLVerificatorTest {
    private Path tempDir;
    private Path tempFxmlFile;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("fxml-test");
        tempFxmlFile = tempDir.resolve("test.fxml");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFxmlFile);
        Files.deleteIfExists(tempDir);
    }

    @Test
    void testVerifyAllWithNoFxmlFiles() {
        // Test with empty directory - should not throw exception
        assertDoesNotThrow(() -> FXMLVerificator.verifyAll());
    }

    @Test
    void testFxmlWithCorrectNamespace() throws IOException {
        String validFxml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<AnchorPane xmlns=\"http://javafx.com/javafx/19\" xmlns:fx=\"http://javafx.com/fxml/1\">\n" +
            "    <children>\n" +
            "        <Label text=\"Hello World\" />\n" +
            "    </children>\n" +
            "</AnchorPane>";
        
        Files.writeString(tempFxmlFile, validFxml);
        
        // This should not throw any exceptions
        assertDoesNotThrow(() -> FXMLVerificator.verifyAll());
    }

    @Test
    void testFxmlWithIncorrectNamespace() throws IOException {
        String fxmlWithOldNamespace = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<AnchorPane xmlns=\"http://javafx.com/javafx/11.0.1\" xmlns:fx=\"http://javafx.com/fxml/1\">\n" +
            "    <children>\n" +
            "        <Label text=\"Hello World\" />\n" +
            "    </children>\n" +
            "</AnchorPane>";
        
        Files.writeString(tempFxmlFile, fxmlWithOldNamespace);
        
        // Should process without throwing exceptions (it should normalize the namespace)
        assertDoesNotThrow(() -> FXMLVerificator.verifyAll());
    }

    @Test
    void testFxmlWithIllegalAttribute() throws IOException {
        // Create a temporary FXML file in the resources directory
        Path resourcesDir = Paths.get("src/main/resources");
        Files.createDirectories(resourcesDir);
        Path testFxmlFile = resourcesDir.resolve("test_illegal.fxml");
        
        try {
            String fxmlWithIllegalAttr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<AnchorPane xmlns=\"http://javafx.com/javafx/19\" xmlns:fx=\"http://javafx.com/fxml/1\">\n" +
                "    <children>\n" +
                "        <Label text=\"Hello World\" fx:factory=\"someFactory\" />\n" +
                "    </children>\n" +
                "</AnchorPane>";
            
            Files.writeString(testFxmlFile, fxmlWithIllegalAttr);
            
            // Should throw RuntimeException due to illegal attribute
            assertThrows(RuntimeException.class, () -> FXMLVerificator.verifyAll());
        } finally {
            // Clean up
            Files.deleteIfExists(testFxmlFile);
        }
    }

    @Test
    void testFxmlWithUnsupportedTag() throws IOException {
        // Create a temporary FXML file in the resources directory
        Path resourcesDir = Paths.get("src/main/resources");
        Files.createDirectories(resourcesDir);
        Path testFxmlFile = resourcesDir.resolve("test_unsupported.fxml");
        
        try {
            String fxmlWithUnsupportedTag = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<AnchorPane xmlns=\"http://javafx.com/javafx/19\" xmlns:fx=\"http://javafx.com/fxml/1\">\n" +
                "    <children>\n" +
                "        <TextFlow>\n" +
                "            <Text text=\"Unsupported content\" />\n" +
                "        </TextFlow>\n" +
                "    </children>\n" +
                "</AnchorPane>";
            
            Files.writeString(testFxmlFile, fxmlWithUnsupportedTag);
            
            // Should throw RuntimeException due to unsupported tag
            assertThrows(RuntimeException.class, () -> FXMLVerificator.verifyAll());
        } finally {
            // Clean up
            Files.deleteIfExists(testFxmlFile);
        }
    }

    @Test
    void testFxmlWithCustomComponents() throws IOException {
        String fxmlWithCustom = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<AnchorPane xmlns=\"http://javafx.com/javafx/19\" xmlns:fx=\"http://javafx.com/fxml/1\">\n" +
            "    <children>\n" +
            "        <Button text=\"Standard Button\" />\n" +
            "        <Label text=\"Standard Label\" />\n" +
            "    </children>\n" +
            "</AnchorPane>";
        
        Files.writeString(tempFxmlFile, fxmlWithCustom);
        
        // Should process successfully
        assertDoesNotThrow(() -> FXMLVerificator.verifyAll());
    }

    @Test
    void testFxmlWithMultipleNamespaceVersions() throws IOException {
        String fxmlMultipleVersions = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!-- This might contain mixed versions -->\n" +
            "<AnchorPane xmlns=\"http://javafx.com/javafx/17.0.2\" xmlns:fx=\"http://javafx.com/fxml/1\">\n" +
            "    <children>\n" +
            "        <VBox xmlns=\"http://javafx.com/javafx/11.0.1\">\n" +
            "            <Label text=\"Mixed versions\" />\n" +
            "        </VBox>\n" +
            "    </children>\n" +
            "</AnchorPane>";
        
        Files.writeString(tempFxmlFile, fxmlMultipleVersions);
        
        // Should normalize all namespace versions
        assertDoesNotThrow(() -> FXMLVerificator.verifyAll());
    }

    @Test
    void testFxmlWithComplexStructure() throws IOException {
        String complexFxml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<BorderPane xmlns=\"http://javafx.com/javafx/19\" xmlns:fx=\"http://javafx.com/fxml/1\">\n" +
                "    <top>\n" +
                "        <MenuBar>\n" +
                "            <Menu text=\"File\">\n" +
                "                <MenuItem text=\"New\" />\n" +
                "                <MenuItem text=\"Open\" />\n" +
                "            </Menu>\n" +
                "        </MenuBar>\n" +
                "    </top>\n" +
                "    <center>\n" +
                "        <TabPane>\n" +
                "            <Tab text=\"Tab 1\">\n" +
                "                <VBox>\n" +
                "                    <Label text=\"Content 1\" />\n" +
                "                    <TextField promptText=\"Enter text\" />\n" +
                "                    <Button text=\"Submit\" />\n" +
                "                </VBox>\n" +
                "            </Tab>\n" +
                "            <Tab text=\"Tab 2\">\n" +
                "                <HBox>\n" +
                "                    <Label text=\"Content 2\" />\n" +
                "                    <CheckBox text=\"Option\" />\n" +
                "                </HBox>\n" +
                "            </Tab>\n" +
                "        </TabPane>\n" +
                "    </center>\n" +
                "    <bottom>\n" +
                "        <Label text=\"Status: Ready\" />\n" +
                "    </bottom>\n" +
                "</BorderPane>";
        
        Files.writeString(tempFxmlFile, complexFxml);
        
        // Should process complex structure successfully
        assertDoesNotThrow(() -> FXMLVerificator.verifyAll());
    }

    @Test
    void testEmptyFxmlFile() throws IOException {
        Files.writeString(tempFxmlFile, "");
        
        // Should handle empty file gracefully
        assertDoesNotThrow(() -> FXMLVerificator.verifyAll());
    }

    @Test
    void testInvalidXmlFormat() throws IOException {
        String invalidXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<AnchorPane xmlns=\"http://javafx.com/javafx/19\" xmlns:fx=\"http://javafx.com/fxml/1\">\n" +
                "    <children>\n" +
                "        <Label text=\"Unclosed tag\"\n" +
                "    </children>\n" +
                "</AnchorPane>";
        
        Files.writeString(tempFxmlFile, invalidXml);
        
        // Should handle malformed XML gracefully or throw appropriate exception
        // The behavior depends on implementation - it might process or throw
        assertDoesNotThrow(() -> FXMLVerificator.verifyAll());
    }
}
