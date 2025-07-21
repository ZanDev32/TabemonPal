package com.starlight.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.starlight.api.ChatbotAPI;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Test class for SettingController.
 */
public class SettingControllerTest {
    
    @TempDir
    Path tempDir;
    
    private XStream xstream;
    
    @BeforeEach
    public void setUp() {
        this.xstream = new XStream(new DomDriver());
        this.xstream.allowTypesByWildcard(new String[]{"com.starlight.api.*"});
        this.xstream.alias("config", ChatbotAPI.Config.class);
        this.xstream.aliasField("openai-key", ChatbotAPI.Config.class, "openaiKey");
    }
    
    @Test
    public void testConfigCreationAndSerialization() throws Exception {
        // Create config object
        ChatbotAPI.Config config = new ChatbotAPI.Config();
        config.setOpenaiKey("test-api-key-123");
        
        // Test serialization
        String xml = xstream.toXML(config);
        assertTrue(xml.contains("test-api-key-123"));
        assertTrue(xml.contains("<config>"));
        assertTrue(xml.contains("<openai-key>"));
        
        // Test deserialization
        ChatbotAPI.Config deserializedConfig = (ChatbotAPI.Config) xstream.fromXML(xml);
        assertEquals("test-api-key-123", deserializedConfig.getOpenaiKey());
    }
    
    @Test
    public void testConfigFileCreation() throws Exception {
        // Create the directory structure
        Path configDir = tempDir.resolve(".tabemonpal").resolve("Database");
        Files.createDirectories(configDir);
        
        // Create config object
        ChatbotAPI.Config config = new ChatbotAPI.Config();
        config.setOpenaiKey("sk-test-key-123");
        
        // Save to file
        File configFile = configDir.resolve(".SECRET_KEY.xml").toFile();
        xstream.toXML(config, Files.newOutputStream(configFile.toPath()));
        
        // Verify file exists and has correct content
        assertTrue(configFile.exists());
        
        // Load back and verify
        ChatbotAPI.Config loadedConfig = (ChatbotAPI.Config) xstream.fromXML(configFile);
        assertEquals("sk-test-key-123", loadedConfig.getOpenaiKey());
    }
    
    @Test
    public void testEmptyApiKeyValidation() {
        ChatbotAPI.Config config = new ChatbotAPI.Config();
        assertNull(config.getOpenaiKey());
        
        config.setOpenaiKey("");
        assertEquals("", config.getOpenaiKey());
        
        config.setOpenaiKey("   ");
        assertEquals("   ", config.getOpenaiKey());
    }
}
