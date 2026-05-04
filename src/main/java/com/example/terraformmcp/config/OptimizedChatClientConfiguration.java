package com.example.terraformmcp.config;

import com.example.terraformmcp.service.ChatMemoryService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class OptimizedChatClientConfiguration {

    @Bean
    @Primary
    public ChatMemory optimizedChatMemory() {
        return new ChatMemoryService();
    }

    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel, ToolCallbackProvider tools) {
        return ChatClient.builder(chatModel)
                .defaultTools(tools)
                .build();
    }
}