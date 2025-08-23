package com.starlight.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Data model representing a chat message in the conversation history.
 */
public class ChatMessage {
    /** The message content. */
    private String content;
    
    /** True if this message is from the user, false if from the AI assistant. */
    private boolean isUser;
    
    /** Timestamp when the message was created. */
    private String timestamp;
    
    /** The user who sent the message (for user messages). */
    private String username;
    public static final String AI_USERNAME = "TabemonPal AI";
    
    /**
     * Default constructor for XML serialization.
     */
    public ChatMessage() {
    }
    
    /**
     * Constructor for creating a new chat message.
     * 
     * @param content The message content
     * @param isUser True if this is a user message, false for AI message
     * @param username The username of the sender (for user messages)
     */
    public ChatMessage(String content, boolean isUser, String username) {
        this.content = content;
        this.isUser = isUser;
        this.username = isUser ? username : AI_USERNAME;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    // Accessors
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isUser() { return isUser; }
    public void setUser(boolean user) { isUser = user; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    /**
     * Gets a formatted timestamp for display.
     * 
     * @return Formatted timestamp string
     */
    public String getFormattedTimestamp() {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
        } catch (Exception e) {
            return timestamp; // Return original if parsing fails
        }
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s: %s", 
            getFormattedTimestamp(), 
            isUser ? username : AI_USERNAME, 
            content.substring(0, Math.min(50, content.length())) + (content.length() > 50 ? "..." : ""));
    }
}
