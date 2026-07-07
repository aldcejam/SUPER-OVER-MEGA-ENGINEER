package com.supersys.ai.controller.graphql;

import com.supersys.ai.service.DatabaseChatService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class DatabaseChatController {

    private final DatabaseChatService databaseChatService;

    @Autowired
    public DatabaseChatController(DatabaseChatService databaseChatService) {
        this.databaseChatService = databaseChatService;
    }

    @MutationMapping
    @RateLimiter(name = "aiAnalysisLimiter")
    public DatabaseChatResponseGql chatWithDatabase(
            @Argument String message,
            @Argument String conversationId) {
        try {
            DatabaseChatService.DatabaseChatResponse response =
                    databaseChatService.chat(message, conversationId);
            return new DatabaseChatResponseGql(response.answer(), response.conversationId());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao processar chat com banco: " + e.getClass().getName() + " - " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public List<DatabaseToolGql> listDatabaseTools() {
        return databaseChatService.listAvailableTools().stream()
                .map(t -> new DatabaseToolGql(t.name(), t.description()))
                .toList();
    }

    public record DatabaseChatResponseGql(String answer, String conversationId) {}

    public record DatabaseToolGql(String name, String description) {}
}
