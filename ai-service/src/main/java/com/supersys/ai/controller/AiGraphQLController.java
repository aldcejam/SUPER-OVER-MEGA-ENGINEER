package com.supersys.ai.controller;

import com.supersys.ai.dto.ScheduleDto;
import com.supersys.ai.dto.ScheduleAnalysisResponseDto;
import com.supersys.ai.service.ScheduleAnalysisService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

@Controller
public class AiGraphQLController {

    private final ChatModel chatModel;
    private final VectorStore vectorStore;
    private final ScheduleAnalysisService scheduleAnalysisService;
    private final ChatClient chatClient;
    private final List<ToolCallbackProvider> toolProviders;

    @Autowired
    public AiGraphQLController(ChatModel chatModel, VectorStore vectorStore, ScheduleAnalysisService scheduleAnalysisService, ChatClient.Builder chatClientBuilder, @Autowired(required = false) List<ToolCallbackProvider> toolProviders) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
        this.scheduleAnalysisService = scheduleAnalysisService;
        this.chatClient = chatClientBuilder.build();
        this.toolProviders = toolProviders != null ? toolProviders : List.of();
    }

    @QueryMapping
    public AiResponse askProjectQuestion(@Argument String prompt) {
        String systemPrompt = "Você é um assistente de desenvolvimento sênior respondendo a perguntas sobre o projeto." +
                              " O repositório deste projeto no GitHub é 'aldcejam/SUPER-OVER-MEGA-ENGINEER'." +
                              " Você deve utilizar as ferramentas do GitHub (GitHub MCP tools) disponíveis para buscar informações no repositório ou responder à pergunta do usuário.";
        
        ToolCallback[] callbacks = toolProviders.stream()
                .flatMap(p -> Arrays.stream(p.getToolCallbacks()))
                .toArray(ToolCallback[]::new);

        String answer = this.chatClient.prompt()
                .system(systemPrompt)
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
            // Fallback gracefully in case pgvector is bootstrapping/initializing
            context = "Erro ou base de vetores vazia. Utilizando base padrão do modelo.";
        }

        // Construct the context-enriched prompt (RAG Flow)
        String enrichedPrompt = "Você é o assistente virtual da arquitetura distribuída SUPER-SYS.\n" +
                "Utilize as informações do contexto abaixo (extraído da base semântica pgvector) para responder:\n\n" +
                "--- CONTEXTO ---\n" + context + "\n----------------\n\n" +
                "Pergunta do Usuário: " + prompt + "\n\n" +
                "Resposta Clara e Sucinta:";

        ChatResponse chatResponse = this.chatModel.call(new Prompt(enrichedPrompt));
        String answer = chatResponse.getResult().getOutput().getText();

        return new AiResponse(answer);
    }

    @MutationMapping
    public ScheduleAnalysisResponseDto analyzeSchedule(@Argument ScheduleDto schedule) {
        return scheduleAnalysisService.analyze(schedule);
    }

    public record AiResponse(String answer) {}
}


