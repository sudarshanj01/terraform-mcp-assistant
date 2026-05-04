package com.example.terraformmcp.service;

import com.example.terraformmcp.model.ChatMessage;
import com.example.terraformmcp.model.ConversationSession;
import com.example.terraformmcp.model.ConversationSummary;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.io.*;
import java.nio.file.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Service
public class ChatMemoryService implements ChatMemory {

    public ChatMemoryService() {
        this(100, 50, 60, true, "./chat-history");
    }

    private final Map<String, ConversationSession> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final ObjectMapper objectMapper;
    private final Path persistenceDirectory;

    // Configuration
    private final int maxMessagesPerSession;
    private final int maxSessionsInMemory;
    private final long sessionTimeoutMinutes;
    private final boolean persistenceEnabled;

    public ChatMemoryService(
            @Value("${chat.memory.max-messages:100}") int maxMessagesPerSession,
            @Value("${chat.memory.max-sessions:50}") int maxSessionsInMemory,
            @Value("${chat.memory.session-timeout:60}") long sessionTimeoutMinutes,
            @Value("${chat.memory.persistence.enabled:true}") boolean persistenceEnabled,
            @Value("${chat.memory.persistence.directory:./chat-history}") String persistenceDir) {

        this.maxMessagesPerSession = maxMessagesPerSession;
        this.maxSessionsInMemory = maxSessionsInMemory;
        this.sessionTimeoutMinutes = sessionTimeoutMinutes;
        this.persistenceEnabled = persistenceEnabled;
        this.persistenceDirectory = Paths.get(persistenceDir);

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());

        initializePersistence();
        startMaintenanceTasks();
    }

    private void initializePersistence() {
        if (persistenceEnabled) {
            try {
                Files.createDirectories(persistenceDirectory);
                clearIncompatibleSessions(); // Clear old incompatible files
                loadPersistedSessions();
            } catch (IOException e) {
                System.err.println("Failed to initialize persistence: " + e.getMessage());
            }
        }
    }

    private void clearIncompatibleSessions() {
        try {
            if (Files.exists(persistenceDirectory)) {
                Files.list(persistenceDirectory)
                        .filter(path -> path.toString().endsWith(".json"))
                        .forEach(path -> {
                            try {
                                // Try to read the file, if it fails, delete it
                                objectMapper.readValue(path.toFile(), ConversationSession.class);
                            } catch (Exception e) {
                                try {
                                    Files.delete(path);
                                    System.out.println("Deleted incompatible session file: " + path.getFileName());
                                } catch (IOException deleteEx) {
                                    System.err.println("Failed to delete incompatible file: " + deleteEx.getMessage());
                                }
                            }
                        });
            }
        } catch (IOException e) {
            System.err.println("Failed to clean incompatible sessions: " + e.getMessage());
        }
    }

    private void startMaintenanceTasks() {
        // Cleanup expired sessions every 10 minutes
        scheduler.scheduleAtFixedRate(this::cleanupExpiredSessions, 10, 10, TimeUnit.MINUTES);

        // Persist active sessions every 5 minutes
        if (persistenceEnabled) {
            scheduler.scheduleAtFixedRate(this::persistActiveSessions, 5, 5, TimeUnit.MINUTES);
        }
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        ConversationSession session = getOrCreateSession(conversationId);
        synchronized (session) {
            for (Message message : messages) {
                session.addMessage(new ChatMessage(message, LocalDateTime.now()));
            }
            session.updateLastAccess();

            // Trim if exceeds max messages
            if (session.getMessageCount() > maxMessagesPerSession) {
                session.trimToSize(maxMessagesPerSession);
            }
        }

        // Persist immediately for important conversations
        if (persistenceEnabled && messages.size() > 0) {
            persistSessionAsync(conversationId, session);
        }
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        ConversationSession session = sessions.get(conversationId);

        if (session == null && persistenceEnabled) {
            session = loadPersistedSession(conversationId);
        }

        if (session == null) {
            return new ArrayList<>();
        }

        synchronized (session) {
            session.updateLastAccess();
            return session.getRecentMessages(lastN).stream()
                    .map(ChatMessage::getMessage)
                    .toList();
        }
    }

    @Override
    public void clear(String conversationId) {
        sessions.remove(conversationId);
        if (persistenceEnabled) {
            deletePersistedSession(conversationId);
        }
    }

    // Enhanced session management
    private ConversationSession getOrCreateSession(String conversationId) {
        return sessions.computeIfAbsent(conversationId, id -> {
            // Check if we need to evict old sessions
            if (sessions.size() >= maxSessionsInMemory) {
                evictOldestSession();
            }
            return new ConversationSession(id);
        });
    }

    private void evictOldestSession() {
        sessions.entrySet().stream()
                .min(Map.Entry.comparingByValue(
                        (s1, s2) -> s1.getLastAccess().compareTo(s2.getLastAccess())))
                .ifPresent(entry -> {
                    String sessionId = entry.getKey();
                    ConversationSession session = entry.getValue();

                    // Persist before evicting
                    if (persistenceEnabled) {
                        persistSessionAsync(sessionId, session);
                    }
                    sessions.remove(sessionId);
                });
    }

    private void cleanupExpiredSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(sessionTimeoutMinutes);

        sessions.entrySet().removeIf(entry -> {
            ConversationSession session = entry.getValue();
            if (session.getLastAccess().isBefore(cutoff)) {
                // Persist before cleanup
                if (persistenceEnabled) {
                    persistSessionAsync(entry.getKey(), session);
                }
                return true;
            }
            return false;
        });
    }

    // Persistence methods
    private void persistSessionAsync(String sessionId, ConversationSession session) {
        scheduler.execute(() -> persistSession(sessionId, session));
    }

    private void persistSession(String sessionId, ConversationSession session) {
        if (!persistenceEnabled) return;

        try {
            Path sessionFile = persistenceDirectory.resolve(sessionId + ".json");
            objectMapper.writeValue(sessionFile.toFile(), session);
        } catch (IOException e) {
            System.err.println("Failed to persist session " + sessionId + ": " + e.getMessage());
        }
    }

    private void persistActiveSessions() {
        sessions.forEach(this::persistSessionAsync);
    }

    private ConversationSession loadPersistedSession(String sessionId) {
        if (!persistenceEnabled) return null;

        try {
            Path sessionFile = persistenceDirectory.resolve(sessionId + ".json");
            if (Files.exists(sessionFile)) {
                ConversationSession session = objectMapper.readValue(sessionFile.toFile(), ConversationSession.class);
                sessions.put(sessionId, session); // Cache in memory
                return session;
            }
        } catch (IOException e) {
            System.err.println("Failed to load session " + sessionId + ": " + e.getMessage());
        }
        return null;
    }

    private void loadPersistedSessions() {
        if (!persistenceEnabled) return;

        try {
            Files.list(persistenceDirectory)
                    .filter(path -> path.toString().endsWith(".json"))
                    .limit(maxSessionsInMemory / 2) // Only load half of max to leave room
                    .forEach(path -> {
                        String sessionId = path.getFileName().toString().replace(".json", "");
                        loadPersistedSession(sessionId);
                    });
        } catch (IOException e) {
            System.err.println("Failed to load persisted sessions: " + e.getMessage());
        }
    }

    private void deletePersistedSession(String sessionId) {
        if (!persistenceEnabled) return;

        try {
            Path sessionFile = persistenceDirectory.resolve(sessionId + ".json");
            Files.deleteIfExists(sessionFile);
        } catch (IOException e) {
            System.err.println("Failed to delete session " + sessionId + ": " + e.getMessage());
        }
    }

    public ConversationSummary getConversationSummary(String conversationId) {
        ConversationSession session = sessions.get(conversationId);
        if (session == null && persistenceEnabled) {
            session = loadPersistedSession(conversationId);
        }

        if (session == null) {
            return new ConversationSummary(conversationId, 0, null, null, new ArrayList<>());
        }

        synchronized (session) {
            List<ChatMessage> messages = session.getAllMessages();

            List<String> recentPreviews = new ArrayList<>();
            for (ChatMessage msg : messages) {
                if (recentPreviews.size() >= 5) break;
                String content = msg.getMessage().getText();
                recentPreviews.add(content != null ? content : "");
            }

            return new ConversationSummary(
                    conversationId,
                    messages.size(),
                    session.getCreatedAt(),
                    session.getLastAccess(),
                    recentPreviews
            );
        }
    }

    public List<String> getActiveSessionIds() {
        return new ArrayList<>(sessions.keySet());
    }

    public void exportConversation(String conversationId, String format) {
        // Implementation for exporting conversations in different formats
        ConversationSession session = sessions.get(conversationId);
        if (session == null && persistenceEnabled) {
            session = loadPersistedSession(conversationId);
        }

        if (session != null) {
            // Export logic here (JSON, CSV, etc.)
        }
    }
}