package com.supersys.lambda.config;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import com.supersys.lambda.client.AiServiceClient;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Configuration
public class LambdaFunctionsConfig {

    private static final String BUCKET_NAME = "pdf-extractions";

    @Bean
    public Function<Message<byte[]>, String> lambdaService(AiServiceClient aiServiceClient, S3Client s3Client) {
        return message -> {
            byte[] pdfBytes = message.getPayload();
            if (pdfBytes == null || pdfBytes.length == 0) {
                return "Erro: O arquivo PDF enviado esta vazio.";
            }
            
            boolean isDeepAnalysis = false;
            Object deepAnalysisHeader = message.getHeaders().get("deepanalysis");
            if (deepAnalysisHeader == null) {
                deepAnalysisHeader = message.getHeaders().get("deepAnalysis");
            }
            if (deepAnalysisHeader != null) {
                isDeepAnalysis = Boolean.parseBoolean(deepAnalysisHeader.toString());
            }

            Object docIdHeader = message.getHeaders().get("documentid");
            if (docIdHeader == null) {
                docIdHeader = message.getHeaders().get("documentId");
            }
            String docId = docIdHeader != null ? docIdHeader.toString() : UUID.randomUUID().toString();

            if (isDeepAnalysis) {
                Thread.startVirtualThread(() -> {
                    try {
                        aiServiceClient.extractPdfContent(pdfBytes, docId);
                        System.out.println("Arquivo enviado com sucesso para o ai-service com ID: " + docId);
                    } catch (Exception e) {
                        System.err.println("Erro ao enviar arquivo para o ai-service: " + e.getMessage());
                    }
                });

                return "Arquivo encaminhado para análise profunda no ai-service. O processo continuará em background.";
            }

            try (PDDocument document = Loader.loadPDF(pdfBytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);

                if (text == null || text.trim().isEmpty()) {
                    return "Aviso: Nao foi possivel extrair texto do PDF enviado (pode ser digitalizado/imagem).";
                }
                
                String markdownText = "# Arquivo: " + docId + "\n\n" + text;
                byte[] mdBytes = markdownText.getBytes(StandardCharsets.UTF_8);

                String objectKey = docId + ".md";
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(objectKey)
                    .metadata(Map.of(
                        "description", "Analise Rasa Local",
                        "original_size", String.valueOf(pdfBytes.length),
                        "source", "lambda-service"
                    ))
                    .build();
                    
                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(mdBytes));

                return "PDF processado com sucesso (análise rasa). " + text.length() + " caracteres extraídos e salvos no S3 como " + objectKey;
            } catch (Exception e) {
                e.printStackTrace();
                return "Erro ao processar PDF localmente: " + e.getMessage();
            }
        };
    }
}
