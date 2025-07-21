package com.starlight.util;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.starlight.model.ChatHistory;
import com.starlight.model.ChatMessage;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Utility class for managing chat history persistence.
 */
public class ChatHistoryManager {
    private static final Logger logger = Logger.getLogger(ChatHistoryManager.class.getName());
    private static final String CHAT_HISTORY_DIR = "ChatHistory";
    private static final String FILE_EXTENSION = ".xml";
    
    private final XStream xstream;
    private ChatHistory currentSession;
    
    public ChatHistoryManager() {
        // Initialize XStream for XML serialization
        this.xstream = new XStream(new DomDriver());
        this.xstream.allowTypesByWildcard(new String[]{
            "com.starlight.model.*"
        });
        this.xstream.alias("chatHistory", ChatHistory.class);
        this.xstream.alias("message", ChatMessage.class);
        this.xstream.aliasField("messages", ChatHistory.class, "messages");
    }
    
    /**
     * Starts a new chat session for the given user.
     * 
     * @param username The username of the user starting the session
     */
    public void startSession(String username) {
        // End current session if exists
        if (currentSession != null) {
            endSession();
        }
        
        currentSession = new ChatHistory(username);
        logger.info("Started new chat session for user: " + username);
    }
    
    /**
     * Adds a message to the current session.
     * 
     * @param content The message content
     * @param isUser True if this is a user message, false for AI message
     * @param username The username (for user messages)
     */
    public void addMessage(String content, boolean isUser, String username) {
        if (currentSession == null) {
            logger.warning("No active session. Starting new session for user: " + username);
            startSession(username);
        }
        
        ChatMessage message = new ChatMessage(content, isUser, username);
        currentSession.addMessage(message);
        
        // Auto-save after each message (optional, can be changed to periodic saves)
        saveCurrentSession();
    }
    
    /**
     * Ends the current session and saves it to disk.
     */
    public void endSession() {
        if (currentSession == null) {
            return;
        }
        
        currentSession.endSession();
        saveCurrentSession();
        
        logger.info("Ended chat session: " + currentSession.getSessionSummary());
        currentSession = null;
    }
    
    /**
     * Saves the current session to disk.
     */
    private void saveCurrentSession() {
        if (currentSession == null) {
            return;
        }
        
        try {
            String userHome = System.getProperty("user.home");
            Path historyDir = Paths.get(userHome, ".tabemonpal", "Database", CHAT_HISTORY_DIR);
            
            // Create directory if it doesn't exist
            Files.createDirectories(historyDir);
            
            // Create filename based on session ID
            String filename = currentSession.sessionId + FILE_EXTENSION;
            Path historyFile = historyDir.resolve(filename);
            
            // Save to XML file
            String xml = xstream.toXML(currentSession);
            try (FileOutputStream fos = new FileOutputStream(historyFile.toFile())) {
                fos.write(xml.getBytes());
            }
            
            logger.fine("Saved chat history: " + historyFile);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to save chat history: " + e.getMessage(), e);
        }
    }
    
    /**
     * Loads all chat history files for a specific user.
     * 
     * @param username The username to load history for
     * @return List of chat histories, sorted by date (newest first)
     */
    public List<ChatHistory> loadUserHistory(String username) {
        List<ChatHistory> histories = new ArrayList<>();
        
        try {
            String userHome = System.getProperty("user.home");
            Path historyDir = Paths.get(userHome, ".tabemonpal", "Database", CHAT_HISTORY_DIR);
            
            if (!Files.exists(historyDir)) {
                return histories; // Empty list if directory doesn't exist
            }
            
            // Load all XML files in the directory
            Files.list(historyDir)
                .filter(path -> path.toString().endsWith(FILE_EXTENSION))
                .filter(path -> username == null || path.getFileName().toString().startsWith(username + "_"))
                .forEach(path -> {
                    try {
                        String xml = Files.readString(path);
                        ChatHistory history = (ChatHistory) xstream.fromXML(xml);
                        if (history != null) {
                            histories.add(history);
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Failed to load chat history file: " + path, e);
                    }
                });
            
            // Sort by session start time (newest first)
            histories.sort(Comparator.comparing((ChatHistory h) -> h.sessionStart).reversed());
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to load chat histories: " + e.getMessage(), e);
        }
        
        return histories;
    }
    
    /**
     * Loads the most recent chat history for a user.
     * 
     * @param username The username to load history for
     * @return The most recent chat history, or null if none exists
     */
    public ChatHistory loadRecentHistory(String username) {
        List<ChatHistory> histories = loadUserHistory(username);
        return histories.isEmpty() ? null : histories.get(0);
    }
    
    /**
     * Deletes a specific chat history file.
     * 
     * @param sessionId The session ID to delete
     * @return True if deleted successfully, false otherwise
     */
    public boolean deleteHistory(String sessionId) {
        try {
            String userHome = System.getProperty("user.home");
            Path historyFile = Paths.get(userHome, ".tabemonpal", "Database", CHAT_HISTORY_DIR, sessionId + FILE_EXTENSION);
            
            if (Files.exists(historyFile)) {
                Files.delete(historyFile);
                logger.info("Deleted chat history: " + sessionId);
                return true;
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to delete chat history: " + sessionId, e);
        }
        
        return false;
    }
    
    /**
     * Clears all chat history for a specific user.
     * 
     * @param username The username to clear history for
     * @return Number of files deleted
     */
    public int clearUserHistory(String username) {
        int deletedCount = 0;
        List<ChatHistory> histories = loadUserHistory(username);
        
        for (ChatHistory history : histories) {
            if (deleteHistory(history.sessionId)) {
                deletedCount++;
            }
        }
        
        logger.info("Cleared " + deletedCount + " chat history files for user: " + username);
        return deletedCount;
    }
    
    /**
     * Gets the current active session.
     * 
     * @return Current chat history session, or null if no active session
     */
    public ChatHistory getCurrentSession() {
        return currentSession;
    }
    
    /**
     * Checks if there is an active session.
     * 
     * @return True if there is an active session
     */
    public boolean hasActiveSession() {
        return currentSession != null;
    }
}
