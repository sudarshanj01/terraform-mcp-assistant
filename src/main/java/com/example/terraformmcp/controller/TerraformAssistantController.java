package com.example.terraformmcp.controller;

import com.example.terraformmcp.model.ConversationSummary;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.example.terraformmcp.service.ChatMemoryService;


import java.time.Duration;
import java.util.List;
@RestController
@RequestMapping("/api")
public class TerraformAssistantController {

    private final ChatClient chatClient;
    private final ChatMemoryService chatMemoryService;

    @Autowired
    public TerraformAssistantController(ChatClient chatClient,
                                                 ChatMemoryService chatMemoryService) {
        this.chatClient = chatClient;
        this.chatMemoryService = chatMemoryService;
    }

    @PostMapping(value = "/terraform-assistant", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> generateInfrastructure(
            @RequestBody InfrastructureRequest request,
            @RequestParam(required = true) String sessionId) {

        return chatClient.prompt()
                .user(request.getDescription())
                .system("You are QuickInfra, a full-stack Terraform assistant who will generate production-grade Terraform code, " +
                        "Every time before each action, you will ask for user permission.")
                .advisors(new MessageChatMemoryAdvisor(chatMemoryService, sessionId, 20)) // Reduced context window
                .stream()
                .content()
                .filter(chunk -> chunk != null && !chunk.trim().isEmpty())
                .buffer(Duration.ofMillis(100)) // Faster buffering
                .map(chunks -> String.join("", chunks))
                .timeout(Duration.ofMinutes(5)) // Reduced timeout
                .onErrorResume(error -> Flux.just("Error generating response: " + error.getMessage()));
    }

    // New endpoints for conversation management
    @GetMapping("/conversations/{sessionId}/summary")
    public Mono<ConversationSummary> getConversationSummary(@PathVariable String sessionId) {
        return Mono.fromCallable(() -> chatMemoryService.getConversationSummary(sessionId));
    }

    @GetMapping("/conversations")
    public Mono<List<String>> getActiveConversations() {
        return Mono.fromCallable(() -> chatMemoryService.getActiveSessionIds());
    }

    @DeleteMapping("/conversations/{sessionId}")
    public Mono<Void> clearConversation(@PathVariable String sessionId) {
        return Mono.fromRunnable(() -> chatMemoryService.clear(sessionId));
    }

    @PostMapping("/conversations/{sessionId}/export")
    public Mono<Void> exportConversation(@PathVariable String sessionId,
                                         @RequestParam(defaultValue = "json") String format) {
        return Mono.fromRunnable(() -> chatMemoryService.exportConversation(sessionId, format));
    }

    public static class InfrastructureRequest {
        private String description;

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}