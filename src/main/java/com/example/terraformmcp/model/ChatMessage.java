package com.example.terraformmcp.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.chat.messages.Message;

import java.time.LocalDateTime;

public class ChatMessage {
    private final Message message;
    private final LocalDateTime timestamp;

    @JsonCreator
    public ChatMessage(@JsonProperty("message") Message message,
                       @JsonProperty("timestamp") LocalDateTime timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public Message getMessage() { return message; }


    public LocalDateTime getTimestamp() { return timestamp; }
}
