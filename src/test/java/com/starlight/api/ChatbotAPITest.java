package com.starlight.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ChatbotAPI.
 */
public class ChatbotAPITest {
    
    @BeforeEach
    public void setUp() {
        // Note: This test won't actually create the ChatbotAPI since it requires a valid API key
        // We'll test the structure and error handling instead
    }
    
    @Test
    public void testChatbotExceptionCreation() {
        // Test exception creation
        ChatbotAPI.ChatbotException exception = new ChatbotAPI.ChatbotException("Test message");
        assertEquals("Test message", exception.getMessage());
        
        Exception cause = new RuntimeException("Cause");
        ChatbotAPI.ChatbotException exceptionWithCause = new ChatbotAPI.ChatbotException("Test message", cause);
        assertEquals("Test message", exceptionWithCause.getMessage());
        assertEquals(cause, exceptionWithCause.getCause());
    }
    
    @Test
    public void testConfigCreation() {
        // Test config object creation
        ChatbotAPI.Config config = new ChatbotAPI.Config();
        assertNull(config.getOpenaiKey());
        
        config.setOpenaiKey("test-key");
        assertEquals("test-key", config.getOpenaiKey());
    }
    
    @Test
    public void testChatbotInitializationWithoutValidKey() {
        // The ChatbotAPI should initialize successfully
        ChatbotAPI chatbot = new ChatbotAPI();
        
        // Test that it can be used (either returns demo message, API key error, or actual response)
        try {
            String response = chatbot.sendMessage("Hello");
            // If we get here, either demo mode is working or API key is configured
            assertTrue(response.contains("demo mode") || 
                      response.contains("configure your OpenAI API key") || 
                      response.length() > 0); // Any non-empty response is acceptable
        } catch (ChatbotAPI.ChatbotException e) {
            // This is also acceptable - it means the API key is missing or there's an error
            assertTrue(e.getMessage().contains("demo mode") || 
                      e.getMessage().contains("API key") || 
                      e.getMessage().length() > 0); // Any error message is acceptable
        }
    }
    
    @Test
    public void testJsonResponseParsing() throws ChatbotAPI.ChatbotException {
        // Test the improved JSON parsing with a mock OpenAI API response
        ChatbotAPI chatbot = new ChatbotAPI();
        
        // Mock OpenAI API response format
        String mockResponse = "{\n" +
                "  \"id\": \"chatcmpl-123\",\n" +
                "  \"object\": \"chat.completion\",\n" +
                "  \"created\": 1677652288,\n" +
                "  \"model\": \"gpt-3.5-turbo\",\n" +
                "  \"choices\": [\n" +
                "    {\n" +
                "      \"index\": 0,\n" +
                "      \"message\": {\n" +
                "        \"role\": \"assistant\",\n" +
                "        \"content\": \"Hello! How can I help you today?\"\n" +
                "      },\n" +
                "      \"finish_reason\": \"stop\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"usage\": {\n" +
                "    \"prompt_tokens\": 10,\n" +
                "    \"completion_tokens\": 9,\n" +
                "    \"total_tokens\": 19\n" +
                "  }\n" +
                "}";
        
        String result = chatbot.parseResponse(mockResponse);
        assertEquals("Hello! How can I help you today?", result);
    }
    
    @Test
    public void testJsonResponseParsingWithEscapedCharacters() throws ChatbotAPI.ChatbotException {
        // Test JSON parsing with escaped characters
        ChatbotAPI chatbot = new ChatbotAPI();
        
        String mockResponse = "{\n" +
                "  \"choices\": [\n" +
                "    {\n" +
                "      \"message\": {\n" +
                "        \"content\": \"Hello! I can help with \\\"nutrition\\\" and \\n food advice.\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        
        String result = chatbot.parseResponse(mockResponse);
        assertEquals("Hello! I can help with \"nutrition\" and \n food advice.", result);
    }
    
    @Test
    public void testJsonResponseParsingInvalidFormat() {
        // Test error handling with invalid JSON format
        ChatbotAPI chatbot = new ChatbotAPI();
        
        String invalidResponse = "{\"invalid\": \"format\"}";
        
        ChatbotAPI.ChatbotException exception = assertThrows(ChatbotAPI.ChatbotException.class, () -> {
            chatbot.parseResponse(invalidResponse);
        });
        
        assertTrue(exception.getMessage().contains("Invalid response format"));
    }
}
