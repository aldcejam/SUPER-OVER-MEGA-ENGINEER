package com.supersys.ai.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ai.vectorstore.VectorStore;

@Service
public class AiQueryService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final VectorStore vectorStore;

    @Value("${supersys.ai.prompt.system}")
    private String systemPromptString;

    @Value("${supersys.ai.prompt.user}")
    private String userPromptString;

    @Autowired
    public AiQueryService(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, org.springframework.ai.vectorstore.VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.chatMemory = chatMemory;
        this.vectorStore = vectorStore;
    }

    @CircuitBreaker(name = "aiAnalysisBreaker")
    @RateLimiter(name = "aiAnalysisLimiter")
    public String askDeepSeek(String prompt) {
        return this.chatClient.prompt()
                .system(systemPromptString)
                .user(userPromptString.replace("{prompt}", prompt))
                .advisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).conversationId("default").build(),
                        QuestionAnswerAdvisor.builder(vectorStore).build()
                )
                .call()
                .content();
    }
}
