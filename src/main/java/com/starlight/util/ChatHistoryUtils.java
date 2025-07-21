package com.starlight.util;

import com.starlight.model.ChatHistory;
import com.starlight.model.ChatMessage;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Utility class for chat history operations and display formatting.
 */
public class ChatHistoryUtils {
    
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
            int messageCount = history.messages != null ? history.messages.size() : 0;
            totalMessages += messageCount;
            
            summary.append("Session ").append(i + 1).append(": ").append(history.sessionId).append("\n");
            summary.append("Started: ").append(formatTimestamp(history.sessionStart)).append("\n");
            if (history.sessionEnd != null) {
                summary.append("Ended: ").append(formatTimestamp(history.sessionEnd)).append("\n");
                summary.append("Duration: ").append(history.getSessionDuration()).append("\n");
            } else {
                summary.append("Status: Active\n");
            }
            summary.append("Messages: ").append(messageCount).append("\n");
            
            // Show first message as preview
            if (messageCount > 0) {
                ChatMessage firstMessage = history.messages.get(0);
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
            .filter(h -> h.sessionId.equals(sessionId))
            .findFirst()
            .orElse(null);
            
        if (targetHistory == null) {
            return "Session not found: " + sessionId;
        }
        
        StringBuilder export = new StringBuilder();
        export.append("TabemonPal Chat Export\n");
        export.append("=".repeat(30)).append("\n");
        export.append("User: ").append(targetHistory.username).append("\n");
        export.append("Session: ").append(targetHistory.sessionId).append("\n");
        export.append("Date: ").append(formatTimestamp(targetHistory.sessionStart)).append("\n");
        if (targetHistory.sessionEnd != null) {
            export.append("Duration: ").append(targetHistory.getSessionDuration()).append("\n");
        }
        export.append("\n").append("Conversation:\n");
        export.append("-".repeat(30)).append("\n");
        
        if (targetHistory.messages != null) {
            for (ChatMessage message : targetHistory.messages) {
                String sender = message.isUser ? targetHistory.username : "TabemonPal AI";
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
            
            int totalMsgs = 0;
            int userMsgs = 0;
            int aiMsgs = 0;
            String oldest = null;
            String newest = null;
            
            for (ChatHistory history : histories) {
                if (history.messages != null) {
                    totalMsgs += history.messages.size();
                    for (ChatMessage msg : history.messages) {
                        if (msg.isUser) userMsgs++;
                        else aiMsgs++;
                    }
                }
                
                if (newest == null || (history.sessionStart != null && 
                    history.sessionStart.compareTo(newest) > 0)) {
                    newest = history.sessionStart;
                }
                
                if (oldest == null || (history.sessionStart != null && 
                    history.sessionStart.compareTo(oldest) < 0)) {
                    oldest = history.sessionStart;
                }
            }
            
            this.totalMessages = totalMsgs;
            this.userMessages = userMsgs;
            this.aiMessages = aiMsgs;
            this.oldestSession = oldest;
            this.newestSession = newest;
            this.averageMessagesPerSession = totalSessions > 0 ? 
                (double) totalMessages / totalSessions : 0;
        }
        
        @Override
        public String toString() {
            return String.format(
                "Chat History Statistics:\n" +
                "Total Sessions: %d\n" +
                "Total Messages: %d\n" +
                "User Messages: %d\n" +
                "AI Messages: %d\n" +
                "Average Messages/Session: %.1f\n" +
                "Date Range: %s to %s",
                totalSessions, totalMessages, userMessages, aiMessages,
                averageMessagesPerSession,
                formatTimestamp(oldestSession),
                formatTimestamp(newestSession)
            );
        }
    }
}
