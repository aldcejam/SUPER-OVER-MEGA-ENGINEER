package com.supersys.lambda.config;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import com.supersys.lambda.client.AiServiceClient;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Configuration
public class LambdaFunctionsConfig {

    @Bean
    public Function<Message<byte[]>, String> lambdaService(VectorStore vectorStore, AiServiceClient aiServiceClient) {
        return message -> {
            byte[] pdfBytes = message.getPayload();
            if (pdfBytes == null || pdfBytes.length == 0) {
                return "Erro: O arquivo PDF enviado esta vazio.";
            }
            
            boolean isDeepAnalysis = false;
            Object headerValue = message.getHeaders().get("deepanalysis"); // Lowercase because HTTP headers are mapped case-insensitively usually, or spring cloud function maps it.
            if (headerValue == null) {
                headerValue = message.getHeaders().get("deepAnalysis");
            }
            if (headerValue != null) {
                isDeepAnalysis = Boolean.parseBoolean(headerValue.toString());
            }

            if (isDeepAnalysis) {
                Thread.startVirtualThread(() -> {
                    try {
                        aiServiceClient.extractPdfContent(pdfBytes);
                        System.out.println("Arquivo enviado com sucesso para o ai-service.");
                    } catch (Exception e) {
                        System.err.println("Erro ao enviar arquivo para o ai-service: " + e.getMessage());
                    }
                });

                return "Arquivo encaminhado para análise profunda no ai-service. O processo continuará em background.";
            }

            // Fluxo raso
            try (PDDocument document = Loader.loadPDF(pdfBytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);

                if (text == null || text.trim().isEmpty()) {
                    return "Aviso: Nao foi possivel extrair texto do PDF enviado (pode ser digitalizado/imagem).";
                }

                String docId = UUID.randomUUID().toString();
                Document aiDoc = new Document(
                    docId,
                    text,
                    Map.of(
                        "source", "lambda-service-upload",
                        "timestamp", System.currentTimeMillis()
                    )
                );

                vectorStore.add(List.of(aiDoc));

                return "PDF processado com sucesso (análise rasa). " + text.length() + " caracteres extraídos e inseridos no pgvector.";
            } catch (Exception e) {
                e.printStackTrace();
                return "Erro ao processar PDF: " + e.getMessage();
            }
        };
    }
}
