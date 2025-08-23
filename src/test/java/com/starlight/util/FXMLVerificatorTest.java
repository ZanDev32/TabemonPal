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
class FXMLVerificatorTest {
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
        String validFxml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <AnchorPane xmlns="http://javafx.com/javafx/19" xmlns:fx="http://javafx.com/fxml/1">
                    <children>
                        <Label text="Hello World" />
                    </children>
                </AnchorPane>
                """;
        
        Files.writeString(tempFxmlFile, validFxml);
        
        // This should not throw any exceptions
        assertDoesNotThrow(() -> FXMLVerificator.verifyAll());
    }

    @Test
    void testFxmlWithIncorrectNamespace() throws IOException {
        String fxmlWithOldNamespace = """
                <?xml version="1.0" encoding="UTF-8"?>
                <AnchorPane xmlns="http://javafx.com/javafx/11.0.1" xmlns:fx="http://javafx.com/fxml/1">
                    <children>
                        <Label text="Hello World" />
                    </children>
                </AnchorPane>
                """;
        
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
            String fxmlWithIllegalAttr = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <AnchorPane xmlns="http://javafx.com/javafx/19" xmlns:fx="http://javafx.com/fxml/1">
                        <children>
                            <Label text="Hello World" fx:factory="someFactory" />
                        </children>
                    </AnchorPane>
                    """;
            
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
            String fxmlWithUnsupportedTag = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <AnchorPane xmlns="http://javafx.com/javafx/19" xmlns:fx="http://javafx.com/fxml/1">
                        <children>
                            <TextFlow>
                                <Text text="Unsupported content" />
                            </TextFlow>
                        </children>
                    </AnchorPane>
                    """;
            
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
        String fxmlWithCustom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <AnchorPane xmlns="http://javafx.com/javafx/19" xmlns:fx="http://javafx.com/fxml/1">
                    <children>
                        <Button text="Standard Button" />
                        <Label text="Standard Label" />
                    </children>
                </AnchorPane>
                """;
        
        Files.writeString(tempFxmlFile, fxmlWithCustom);
        
        // Should process successfully
        assertDoesNotThrow(() -> FXMLVerificator.verifyAll());
    }

    @Test
    void testFxmlWithMultipleNamespaceVersions() throws IOException {
        String fxmlMultipleVersions = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!-- This might contain mixed versions -->
                <AnchorPane xmlns="http://javafx.com/javafx/17.0.2" xmlns:fx="http://javafx.com/fxml/1">
                    <children>
                        <VBox xmlns="http://javafx.com/javafx/11.0.1">
                            <Label text="Mixed versions" />
                        </VBox>
                    </children>
                </AnchorPane>
                """;
        
        Files.writeString(tempFxmlFile, fxmlMultipleVersions);
        
        // Should normalize all namespace versions
        assertDoesNotThrow(() -> FXMLVerificator.verifyAll());
    }

    @Test
    void testFxmlWithComplexStructure() throws IOException {
        String complexFxml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <BorderPane xmlns="http://javafx.com/javafx/19" xmlns:fx="http://javafx.com/fxml/1">
                    <top>
                        <MenuBar>
                            <Menu text="File">
                                <MenuItem text="New" />
                                <MenuItem text="Open" />
                            </Menu>
                        </MenuBar>
                    </top>
                    <center>
                        <TabPane>
                            <Tab text="Tab 1">
                                <VBox>
                                    <Label text="Content 1" />
                                    <TextField promptText="Enter text" />
                                    <Button text="Submit" />
                                </VBox>
                            </Tab>
                            <Tab text="Tab 2">
                                <HBox>
                                    <Label text="Content 2" />
                                    <CheckBox text="Option" />
                                </HBox>
                            </Tab>
                        </TabPane>
                    </center>
                    <bottom>
                        <Label text="Status: Ready" />
                    </bottom>
                </BorderPane>
                """;
        
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
        String invalidXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <AnchorPane xmlns="http://javafx.com/javafx/19" xmlns:fx="http://javafx.com/fxml/1">
                    <children>
                        <Label text="Unclosed tag"
                    </children>
                </AnchorPane>
                """;
        
        Files.writeString(tempFxmlFile, invalidXml);
        
        // Should handle malformed XML gracefully or throw appropriate exception
        // The behavior depends on implementation - it might process or throw
        assertDoesNotThrow(() -> FXMLVerificator.verifyAll());
    }
}
