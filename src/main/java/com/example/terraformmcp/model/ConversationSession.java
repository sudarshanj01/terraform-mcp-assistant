package com.example.terraformmcp.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.chat.messages.Message;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConversationSession {
    private final String sessionId;
    private final LocalDateTime createdAt;
    private LocalDateTime lastAccess;
    private final List<ChatMessage> messages;

    @JsonCreator
    public ConversationSession(@JsonProperty("sessionId") String sessionId) {
        this.sessionId = sessionId;
        this.createdAt = LocalDateTime.now();
        this.lastAccess = LocalDateTime.now();
        this.messages = new CopyOnWriteArrayList<>();
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        updateLastAccess();
    }

    public void updateLastAccess() {
        this.lastAccess = LocalDateTime.now();
    }

    public List<ChatMessage> getRecentMessages(int count) {
        int size = messages.size();
        int fromIndex = Math.max(0, size - count);
        return new ArrayList<>(messages.subList(fromIndex, size));
    }

    public List<ChatMessage> getAllMessages() {
        return new ArrayList<>(messages);
    }

    public void trimToSize(int maxSize) {
        if (messages.size() > maxSize) {
            int removeCount = messages.size() - maxSize;
            messages.subList(0, removeCount).clear();
        }
    }

    public int getMessageCount() {
        return messages.size();
    }

    // Getters
    public String getSessionId() { return sessionId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastAccess() { return lastAccess; }
}


