package com.starlight.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.starlight.model.ChatHistory;
import com.starlight.model.ChatMessage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ChatHistoryManager.
 */
class ChatHistoryManagerTest {
    
    private ChatHistoryManager historyManager;
    private final String testUsername = "testuser";
    
    @BeforeEach
    void setUp() {
        historyManager = new ChatHistoryManager();
    }
    
    @Test
    void testSessionLifecycle() {
        // Start a session
        assertFalse(historyManager.hasActiveSession());
        historyManager.startSession(testUsername);
        assertTrue(historyManager.hasActiveSession());
        
        ChatHistory currentSession = historyManager.getCurrentSession();
        assertNotNull(currentSession);
        assertEquals(testUsername, currentSession.getUsername());
        assertNotNull(currentSession.getSessionId());
        assertNotNull(currentSession.getSessionStart());
        assertNull(currentSession.getSessionEnd());
        
        // End the session
        historyManager.endSession();
        assertFalse(historyManager.hasActiveSession());
        assertNull(historyManager.getCurrentSession());
    }
    
    @Test
    void testAddMessage() {
        historyManager.startSession(testUsername);
        
        // Add a user message
        historyManager.addMessage("Hello, TabemonPal!", true, testUsername);
        
        ChatHistory currentSession = historyManager.getCurrentSession();
        assertNotNull(currentSession);
        assertEquals(1, currentSession.getMessages().size());
        
    ChatMessage message = currentSession.getMessages().get(0);
    assertEquals("Hello, TabemonPal!", message.getContent());
    assertTrue(message.isUser());
    assertEquals(testUsername, message.getUsername());
    assertNotNull(message.getTimestamp());
        
        // Add an AI response
        historyManager.addMessage("Hello! How can I help you today?", false, "TabemonPal AI");
        
        assertEquals(2, currentSession.getMessages().size());
    ChatMessage aiMessage = currentSession.getMessages().get(1);
    assertEquals("Hello! How can I help you today?", aiMessage.getContent());
    assertFalse(aiMessage.isUser());
    assertEquals("TabemonPal AI", aiMessage.getUsername());
    }
    
    @Test
    void testAutoStartSession() {
        // Should auto-start session when adding message without active session
        assertFalse(historyManager.hasActiveSession());
        
        historyManager.addMessage("Test message", true, testUsername);
        
        assertTrue(historyManager.hasActiveSession());
        ChatHistory currentSession = historyManager.getCurrentSession();
        assertNotNull(currentSession);
        assertEquals(1, currentSession.getMessages().size());
    }
    
    @Test
    void testChatMessage() {
        ChatMessage userMessage = new ChatMessage("Test content", true, "testuser");
        
    assertEquals("Test content", userMessage.getContent());
    assertTrue(userMessage.isUser());
    assertEquals("testuser", userMessage.getUsername());
    assertNotNull(userMessage.getTimestamp());
    assertNotNull(userMessage.getFormattedTimestamp());
        
        ChatMessage aiMessage = new ChatMessage("AI response", false, "TabemonPal AI");
    assertFalse(aiMessage.isUser());
    assertEquals("TabemonPal AI", aiMessage.getUsername());
    }
    
    @Test
    void testChatHistoryMethods() {
        ChatHistory history = new ChatHistory(testUsername);
        
        assertEquals(testUsername, history.getUsername());
        assertNotNull(history.getSessionStart());
        assertNull(history.getSessionEnd());
        assertNotNull(history.getSessionId());
        assertTrue(history.getSessionId().contains(testUsername));
        
        // Test session ending
        history.endSession();
        assertNotNull(history.getSessionEnd());
        assertNotNull(history.getSessionDuration());
        
        // Test session summary
        String summary = history.getSessionSummary();
        assertTrue(summary.contains(testUsername));
        assertTrue(summary.contains("0 messages"));
    }
}
