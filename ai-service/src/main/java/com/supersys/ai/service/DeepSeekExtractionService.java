package com.supersys.ai.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Service
public class DeepSeekExtractionService {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private ChatModel chatModel;

    private static final String BUCKET_NAME = "pdf-extractions";

    @Async
    public void processPdf(byte[] pdfBytes, String documentId) {
        try {
            System.out.println("Iniciando analise profunda real do PDF via IA...");
            String docId = (documentId != null && !documentId.trim().isEmpty()) ? documentId : UUID.randomUUID().toString();
            
            // 1. Extrair texto bruto usando PDFBox
            String rawText;
            try (PDDocument document = Loader.loadPDF(pdfBytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                rawText = stripper.getText(document);
            }
            
            if (rawText == null || rawText.trim().isEmpty()) {
                throw new RuntimeException("O texto extraido do PDF esta vazio ou o arquivo e uma imagem.");
            }

            // 2. Chamar IA para analise profunda
            String promptMessage = String.format(
                "Voce e o especialista de analise do sistema SUPER-SYS.\n" +
                "Analise o seguinte conteudo extraido de um documento e gere um relatorio completo formatado em Markdown, " +
                "destacando as principais entidades, decisoes, riscos e um resumo executivo.\n\n" +
                "Conteudo do Documento:\n%s",
                rawText
            );

            ChatResponse chatResponse = chatModel.call(new Prompt(promptMessage));
            String markdownContent = chatResponse.getResult().getOutput().getText();
            
            // Prepend metadata
            String finalContent = String.format("# Analise Profunda (ID: %s)\n\n%s", docId, markdownContent);
                
            // 3. Salvar no S3
            byte[] mdBytes = finalContent.getBytes(StandardCharsets.UTF_8);
            String objectKey = docId + ".md";
            
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(objectKey)
                .metadata(Map.of(
                    "description", "Analise de PDF Profunda via IA",
                    "original_size", String.valueOf(pdfBytes.length),
                    "source", "ai-service-chatmodel"
                ))
                .build();
                
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(mdBytes));
            
            System.out.println("Processamento IA concluido. MD " + objectKey + " enviado para o S3.");
            
        } catch (Exception e) {
            System.err.println("Erro durante a analise do PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
