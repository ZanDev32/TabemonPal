package com.starlight.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.starlight.model.ChatHistory;
import com.starlight.model.ChatMessage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ChatHistoryManager.
 */
public class ChatHistoryManagerTest {
    
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
        assertEquals(testUsername, currentSession.username);
        assertNotNull(currentSession.sessionId);
        assertNotNull(currentSession.sessionStart);
        assertNull(currentSession.sessionEnd);
        
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
        assertEquals(1, currentSession.messages.size());
        
        ChatMessage message = currentSession.messages.get(0);
        assertEquals("Hello, TabemonPal!", message.content);
        assertTrue(message.isUser);
        assertEquals(testUsername, message.username);
        assertNotNull(message.timestamp);
        
        // Add an AI response
        historyManager.addMessage("Hello! How can I help you today?", false, "TabemonPal AI");
        
        assertEquals(2, currentSession.messages.size());
        ChatMessage aiMessage = currentSession.messages.get(1);
        assertEquals("Hello! How can I help you today?", aiMessage.content);
        assertFalse(aiMessage.isUser);
        assertEquals("TabemonPal AI", aiMessage.username);
    }
    
    @Test
    void testAutoStartSession() {
        // Should auto-start session when adding message without active session
        assertFalse(historyManager.hasActiveSession());
        
        historyManager.addMessage("Test message", true, testUsername);
        
        assertTrue(historyManager.hasActiveSession());
        ChatHistory currentSession = historyManager.getCurrentSession();
        assertNotNull(currentSession);
        assertEquals(1, currentSession.messages.size());
    }
    
    @Test
    void testChatMessage() {
        ChatMessage userMessage = new ChatMessage("Test content", true, "testuser");
        
        assertEquals("Test content", userMessage.content);
        assertTrue(userMessage.isUser);
        assertEquals("testuser", userMessage.username);
        assertNotNull(userMessage.timestamp);
        assertNotNull(userMessage.getFormattedTimestamp());
        
        ChatMessage aiMessage = new ChatMessage("AI response", false, "TabemonPal AI");
        assertFalse(aiMessage.isUser);
        assertEquals("TabemonPal AI", aiMessage.username);
    }
    
    @Test
    void testChatHistoryMethods() {
        ChatHistory history = new ChatHistory(testUsername);
        
        assertEquals(testUsername, history.username);
        assertNotNull(history.sessionStart);
        assertNull(history.sessionEnd);
        assertNotNull(history.sessionId);
        assertTrue(history.sessionId.contains(testUsername));
        
        // Test session ending
        history.endSession();
        assertNotNull(history.sessionEnd);
        assertNotNull(history.getSessionDuration());
        
        // Test session summary
        String summary = history.getSessionSummary();
        assertTrue(summary.contains(testUsername));
        assertTrue(summary.contains("0 messages"));
    }
}
