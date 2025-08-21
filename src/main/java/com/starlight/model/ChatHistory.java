package com.starlight.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data model representing a complete chat history session.
 */
public class ChatHistory {
    /** List of chat messages in chronological order. */
    List<ChatMessage> messages;
    
    /** Session start timestamp. */
    String sessionStart;

    /** Session end timestamp. */
    String sessionEnd;

    /** Username of the user who participated in this session. */
    String username;
    
    /** Session ID for unique identification. */
    String sessionId;

    /**
     * Gets the messages contained in this chat history.
     *
     * @return list of ChatMessage (may be empty but never null)
     */
    public java.util.List<ChatMessage> getMessages() {
        if (messages == null) {
            return new java.util.ArrayList<>();
        }
        return messages;
    }

    /**
     * Gets the session start timestamp.
     *
     * @return ISO_LOCAL_DATE_TIME string or null
     */
    public String getSessionStart() {
        return sessionStart;
    }

    /**
     * Gets the username associated with this chat history.
     *
     * @return username or null
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the session end timestamp.
     *
     * @return session end timestamp or null if not ended
     */
    public String getSessionEnd() {
        return sessionEnd;
    }

    /**
     * Public accessor for sessionId so external packages can read it.
     *
     * @return session id string
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Default constructor for XML serialization.
     */
    public ChatHistory() {
        this.messages = new ArrayList<>();
    }
    
    /**
     * Constructor for creating a new chat history session.
     * 
     * @param username The username of the participant
     */
    public ChatHistory(String username) {
        this();
        this.username = username;
        this.sessionStart = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.sessionId = generateSessionId();
    }
    
    /**
     * Adds a message to the chat history.
     * 
     * @param message The chat message to add
     */
    public void addMessage(ChatMessage message) {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        messages.add(message);
    }
    
    /**
     * Ends the current session by setting the end timestamp.
     */
    public void endSession() {
        this.sessionEnd = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    
    /**
     * Generates a unique session ID based on timestamp and username.
     * 
     * @return Unique session ID
     */
    private String generateSessionId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("%s_%s", username != null ? username : "anonymous", timestamp);
    }
    
    /**
     * Gets the session duration in a readable format.
     * 
     * @return Duration string or null if session is still active
     */
    public String getSessionDuration() {
        if (sessionStart == null || sessionEnd == null) {
            return null;
        }
        
        try {
            LocalDateTime start = LocalDateTime.parse(sessionStart, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime end = LocalDateTime.parse(sessionEnd, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            long minutes = java.time.Duration.between(start, end).toMinutes();
            if (minutes < 1) {
                return "< 1 minute";
            } else if (minutes < 60) {
                return minutes + " minutes";
            } else {
                long hours = minutes / 60;
                long remainingMinutes = minutes % 60;
                return hours + "h " + remainingMinutes + "m";
            }
        } catch (Exception e) {
            return "Unknown";
        }
    }
    
    /**
     * Gets a summary of the chat session.
     * 
     * @return Summary string
     */
    public String getSessionSummary() {
        int messageCount = messages != null ? messages.size() : 0;
        String startTime = sessionStart != null ? 
            LocalDateTime.parse(sessionStart, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "Unknown";
        
        return String.format("Session: %s - %d messages - Started: %s", 
            sessionId, messageCount, startTime);
    }
    
    @Override
    public String toString() {
        return getSessionSummary();
    }
}
