package com.supersys.ai.controller.graphql;

import com.supersys.ai.service.AiQueryService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.document.Document;
import java.util.stream.Collectors;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Arrays;

@Controller
public class AiGraphQLController {

    private final ChatModel chatModel;
    private final VectorStore vectorStore;
    private final AiQueryService aiQueryService;
    private final ChatClient chatClient;
    private final List<ToolCallbackProvider> toolProviders;

    @Value("${supersys.ai.prompt.project-system}")
    private String projectSystemPromptTemplate;

    @Value("${supersys.ai.prompt.system}")
    private String systemPromptTemplate;

    @Autowired
    public AiGraphQLController(ChatModel chatModel, VectorStore vectorStore, AiQueryService aiQueryService, ChatClient.Builder chatClientBuilder, @Autowired(required = false) List<ToolCallbackProvider> toolProviders) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
        this.aiQueryService = aiQueryService;
        this.chatClient = chatClientBuilder.build();
        this.toolProviders = toolProviders != null ? toolProviders : List.of();
    }

    @QueryMapping
    public AiResponse askProjectQuestion(@Argument String prompt) {
        ToolCallback[] callbacks = toolProviders.stream()
                .flatMap(p -> Arrays.stream(p.getToolCallbacks()))
                .toArray(ToolCallback[]::new);

        String answer = this.chatClient.prompt()
                .system(projectSystemPromptTemplate)
                .user(prompt)
                .toolCallbacks(callbacks)
                .call()
                .content();

        return new AiResponse(answer);
    }

    @QueryMapping
    public AiResponse askDeepSeek(@Argument String prompt) {
        String context = "";
        try {
            List<Document> similarDocuments = this.vectorStore.similaritySearch(
                    SearchRequest.builder().query(prompt).topK(2).build()
            );
            if (similarDocuments != null && !similarDocuments.isEmpty()) {
                context = similarDocuments.stream()
                        .map(Document::getText)
                        .collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            context = "Nenhum contexto adicional encontrado no banco vetorial.";
        }

        String systemPrompt = systemPromptTemplate.replace("{context}", context);

        String answer = this.chatClient.prompt()
                .system(systemPrompt)
                .user(prompt)
                .call()
                .content();

        return new AiResponse(answer);
    }

    public record AiResponse(String answer) {}
}
