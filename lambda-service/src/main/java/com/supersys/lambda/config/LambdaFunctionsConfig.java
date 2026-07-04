package com.supersys.lambda.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Configuration
public class LambdaFunctionsConfig {

    @Bean
    public Function<Message<byte[]>, String> lambdaService(VectorStore vectorStore) {
        return message -> {
            byte[] mdBytes = message.getPayload();
            if (mdBytes == null || mdBytes.length == 0) {
                return "Erro: O arquivo enviado esta vazio.";
            }

            Object docIdHeader = message.getHeaders().get("documentid");
            if (docIdHeader == null) {
                docIdHeader = message.getHeaders().get("documentId");
            }
            String docId = docIdHeader != null ? docIdHeader.toString() : UUID.randomUUID().toString();

            try {
                String markdownText = new String(mdBytes, StandardCharsets.UTF_8);
                
                Document doc = new Document(markdownText, Map.of(
                    "documentId", docId,
                    "source", "direct-upload"
                ));
                
                vectorStore.add(List.of(doc));

                return "Documento processado e inserido no banco vetorial com sucesso. ID: " + docId;
            } catch (Exception e) {
                e.printStackTrace();
                return "Erro ao processar documento: " + e.getMessage();
            }
        };
    }
}
