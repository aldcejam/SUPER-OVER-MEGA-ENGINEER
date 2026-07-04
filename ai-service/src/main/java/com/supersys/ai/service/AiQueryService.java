package com.supersys.ai.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.chat.messages.Message;

@Service
public class AiQueryService {

    private final ChatModel chatModel;
    private final VectorStore vectorStore;

    @Value("${supersys.ai.prompt.system}")
    private String systemPromptString;

    @Value("${supersys.ai.prompt.user}")
    private String userPromptString;

    @Autowired
    public AiQueryService(ChatModel chatModel, VectorStore vectorStore) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
    }

    @CircuitBreaker(name = "aiAnalysisBreaker")
    @RateLimiter(name = "aiAnalysisLimiter")
    public String askDeepSeek(String prompt) {
        String context = "";
        try {
            List<Document> similarDocuments = this.vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(prompt)
                            .topK(2)
                            .build()
            );
            if (similarDocuments != null && !similarDocuments.isEmpty()) {
                context = similarDocuments.stream()
                        .map(Document::getText)
                        .collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            context = "Erro ou base de vetores vazia. Utilizando base padrão do modelo.";
        }

        Message systemMessage = new SystemPromptTemplate(systemPromptString).createMessage(Map.of("context", context));
        Message userMessage = new PromptTemplate(userPromptString).createMessage(Map.of("prompt", prompt));

        ChatResponse chatResponse = this.chatModel.call(new Prompt(List.of(systemMessage, userMessage)));
        return chatResponse.getResult().getOutput().getText();
    }
}
