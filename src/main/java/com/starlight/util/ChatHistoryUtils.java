package com.starlight.util;

import com.starlight.model.ChatHistory;
import com.starlight.model.ChatMessage;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Utility class for chat history operations and display formatting.
 */
public class ChatHistoryUtils {

    private ChatHistoryUtils() {}

    /**
     * Generates a text summary of all chat sessions for a user.
     * 
     * @param username The username to generate summary for
     * @return Formatted text summary of chat history
     */
    public static String generateChatSummary(String username) {
        ChatHistoryManager manager = new ChatHistoryManager();
        List<ChatHistory> histories = manager.loadUserHistory(username);
        
        if (histories.isEmpty()) {
            return "No chat history found for user: " + username;
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("Chat History Summary for ").append(username).append("\n");
        summary.append("=".repeat(50)).append("\n\n");
        
        int totalMessages = 0;
        for (int i = 0; i < histories.size(); i++) {
            ChatHistory history = histories.get(i);
            int messageCount = history.getMessages() != null ? history.getMessages().size() : 0;
            totalMessages += messageCount;
            
            summary.append("Session ").append(i + 1).append(": ").append(history.getSessionId()).append("\n");
            summary.append("Started: ").append(formatTimestamp(history.getSessionStart())).append("\n");
            if (history.getSessionEnd() != null) {
                summary.append("Ended: ").append(formatTimestamp(history.getSessionEnd())).append("\n");
                summary.append("Duration: ").append(history.getSessionDuration()).append("\n");
            } else {
                summary.append("Status: Active\n");
            }
            summary.append("Messages: ").append(messageCount).append("\n");
            
            // Show first message as preview
            if (messageCount > 0) {
                ChatMessage firstMessage = history.getMessages().get(0);
                String preview = firstMessage.content.length() > 50 ? 
                    firstMessage.content.substring(0, 50) + "..." : 
                    firstMessage.content;
                summary.append("Preview: \"").append(preview).append("\"\n");
            }
            summary.append("\n");
        }
        
        summary.append("Total Sessions: ").append(histories.size()).append("\n");
        summary.append("Total Messages: ").append(totalMessages).append("\n");
        
        return summary.toString();
    }
    
    /**
     * Exports a specific chat session to a readable text format.
     * 
     * @param sessionId The session ID to export
     * @param username The username (for loading history)
     * @return Formatted text of the chat session
     */
    public static String exportSessionToText(String sessionId, String username) {
        ChatHistoryManager manager = new ChatHistoryManager();
        List<ChatHistory> histories = manager.loadUserHistory(username);
        
        ChatHistory targetHistory = histories.stream()
            .filter(h -> h.getSessionId().equals(sessionId))
            .findFirst()
            .orElse(null);
            
        if (targetHistory == null) {
            return "Session not found: " + sessionId;
        }
        
        StringBuilder export = new StringBuilder();
        export.append("TabemonPal Chat Export\n");
        export.append("=".repeat(30)).append("\n");
        export.append("User: ").append(targetHistory.getUsername()).append("\n");
        export.append("Session: ").append(targetHistory.getSessionId()).append("\n");
        export.append("Date: ").append(formatTimestamp(targetHistory.getSessionStart())).append("\n");
        if (targetHistory.getSessionEnd() != null) {
            export.append("Duration: ").append(targetHistory.getSessionDuration()).append("\n");
        }
        export.append("\n").append("Conversation:\n");
        export.append("-".repeat(30)).append("\n");
        
        if (targetHistory.getMessages() != null) {
            for (ChatMessage message : targetHistory.getMessages()) {
                String sender = message.isUser ? targetHistory.getUsername() : "TabemonPal AI";
                export.append("[").append(message.getFormattedTimestamp()).append("] ");
                export.append(sender).append(": ");
                export.append(message.content).append("\n\n");
            }
        }
        
        return export.toString();
    }
    
    /**
     * Gets statistics about a user's chat history.
     * 
     * @param username The username to analyze
     * @return ChatHistoryStats object with analysis data
     */
    public static ChatHistoryStats getHistoryStats(String username) {
        ChatHistoryManager manager = new ChatHistoryManager();
        List<ChatHistory> histories = manager.loadUserHistory(username);
        
        return new ChatHistoryStats(histories);
    }
    
    /**
     * Formats timestamp for display.
     * 
     * @param timestamp ISO timestamp string
     * @return Formatted timestamp
     */
    private static String formatTimestamp(String timestamp) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss"));
        } catch (Exception e) {
            return timestamp;
        }
    }
    
    /**
     * Simple statistics class for chat history analysis.
     */
    public static class ChatHistoryStats {
        public final int totalSessions;
        public final int totalMessages;
        public final int userMessages;
        public final int aiMessages;
        public final String oldestSession;
        public final String newestSession;
        public final double averageMessagesPerSession;
        
        public ChatHistoryStats(List<ChatHistory> histories) {
            this.totalSessions = histories.size();

            this.totalMessages = histories.stream()
                .mapToInt(h -> h.getMessages().size())
                .sum();

            this.userMessages = histories.stream()
                .flatMap(h -> h.getMessages().stream())
                .mapToInt(m -> m.isUser ? 1 : 0)
                .sum();

            this.aiMessages = totalMessages - userMessages;

            // Determine oldest and newest session start times
            this.oldestSession = histories.stream()
                .map(ChatHistory::getSessionStart)
                .filter(Objects::nonNull)
                .min(String::compareTo)
                .orElse(null);

            this.newestSession = histories.stream()
                .map(ChatHistory::getSessionStart)
                .filter(Objects::nonNull)
                .max(String::compareTo)
                .orElse(null);

            this.averageMessagesPerSession = totalSessions > 0 ?
                (double) totalMessages / totalSessions : 0.0;
        }
        
        @Override
        public String toString() {
            return String.format("""
                    Chat History Statistics:
                    Total Sessions: %d
                    Total Messages: %d
                    User Messages: %d
                    AI Messages: %d
                    Average Messages/Session: %.1f
                    Date Range: %s to %s
                    """,
                totalSessions, totalMessages, userMessages, aiMessages,
                averageMessagesPerSession,
                formatTimestamp(oldestSession),
                formatTimestamp(newestSession)
            );
        }
    }
}
