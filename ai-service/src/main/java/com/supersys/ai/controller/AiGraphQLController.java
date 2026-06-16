package com.supersys.ai.controller;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AiGraphQLController {

    private final ChatModel chatModel;
    private final VectorStore vectorStore;

    @Autowired
    public AiGraphQLController(ChatModel chatModel, VectorStore vectorStore) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
    }

    @QueryMapping
    public AiResponse askGemini(@Argument String prompt) {
        String context = "";
        try {
            // Perform vector similarity search on pgvector
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

    public record AiResponse(String answer) {}
}
