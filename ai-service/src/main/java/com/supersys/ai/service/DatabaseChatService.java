package com.supersys.ai.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DatabaseChatService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final List<ToolCallbackProvider> toolProviders;

    @Value("${supersys.ai.prompt.database-chat-system}")
    private String systemPrompt;

    @Autowired
    public DatabaseChatService(
            ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory,
            @Autowired(required = false) List<ToolCallbackProvider> toolProviders) {
        this.chatClient = chatClientBuilder.build();
        this.chatMemory = chatMemory;
        this.toolProviders = toolProviders != null ? toolProviders : List.of();
    }

    public List<DatabaseToolInfo> listAvailableTools() {
        return getAllDatabaseCallbacks().stream()
                .map(tc -> new DatabaseToolInfo(
                        tc.getToolDefinition().name(),
                        tc.getToolDefinition().description()))
                .collect(Collectors.toList());
    }

    @RateLimiter(name = "aiAnalysisLimiter")
    @CircuitBreaker(name = "aiAnalysisBreaker")
    public DatabaseChatResponse chat(String message, String conversationId) {
        String resolvedConversationId = (conversationId != null && !conversationId.isBlank())
                ? conversationId
                : UUID.randomUUID().toString();

        ToolCallback[] dbCallbacks = getAllDatabaseCallbacks().toArray(ToolCallback[]::new);

        String enrichedSystemPrompt = buildSystemPrompt(dbCallbacks);

        String answer = chatClient.prompt()
                .system(enrichedSystemPrompt)
                .user(message)
                .toolCallbacks(dbCallbacks)
                .advisors(
                        MessageChatMemoryAdvisor.builder(chatMemory)
                                .conversationId(resolvedConversationId)
                                .build()
                )
                .call()
                .content();

        return new DatabaseChatResponse(answer, resolvedConversationId);
    }

    private List<ToolCallback> getAllDatabaseCallbacks() {
        return toolProviders.stream()
                .flatMap(p -> Arrays.stream(p.getToolCallbacks()))
                .filter(tc -> isDatabaseTool(tc.getToolDefinition().name()))
                .collect(Collectors.toList());
    }

    private boolean isDatabaseTool(String toolName) {
        String lower = toolName.toLowerCase();
        return lower.contains("select") || lower.contains("query") || lower.contains("execute");
    }

    private String buildSystemPrompt(ToolCallback[] callbacks) {
        if (callbacks.length == 0) {
            return systemPrompt;
        }
        StringBuilder toolsSection = new StringBuilder("\n\n## Ferramentas MCP disponíveis no banco de dados:\n");
        for (ToolCallback tc : callbacks) {
            toolsSection.append("- **").append(tc.getToolDefinition().name()).append("**: ")
                    .append(tc.getToolDefinition().description()).append("\n");
        }
        return systemPrompt + toolsSection;
    }

    public record DatabaseChatResponse(String answer, String conversationId) {}

    public record DatabaseToolInfo(String name, String description) {}
}
