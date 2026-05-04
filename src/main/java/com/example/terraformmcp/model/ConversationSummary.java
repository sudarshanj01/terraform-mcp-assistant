package com.example.terraformmcp.model;

import java.time.LocalDateTime;
import java.util.List;

public class ConversationSummary {
    private final String sessionId;
    private final int messageCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime lastAccess;
    private final List<String> recentMessagePreviews;

    public ConversationSummary(String sessionId, int messageCount,
                               LocalDateTime createdAt, LocalDateTime lastAccess,
                               List<String> recentMessagePreviews) {
        this.sessionId = sessionId;
        this.messageCount = messageCount;
        this.createdAt = createdAt;
        this.lastAccess = lastAccess;
        this.recentMessagePreviews = recentMessagePreviews;
    }

    // Getters
    public String getSessionId() { return sessionId; }
    public int getMessageCount() { return messageCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastAccess() { return lastAccess; }
    public List<String> getRecentMessagePreviews() { return recentMessagePreviews; }
}

