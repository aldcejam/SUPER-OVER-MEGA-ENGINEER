package com.supersys.ai.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DeepSeekExtractionService {

    @Autowired
    private S3Client s3Client;

    private static final String BUCKET_NAME = "pdf-extractions";

    @Async
    public void processPdf(byte[] pdfBytes) {
        try {
            System.out.println("Iniciando analise profunda (simulada) do PDF via DeepSeek...");
            
            Thread.sleep(3000);
            
            String markdownContent = """
                # Analise Profunda
                
                Este documento foi processado pela nossa integracao DeepSeek.
                Ele contem referencias a dados complexos extraidos.
                
                ![Diagrama 1](diagrama1.png)
                """;
                
            byte[] fakeImageBytes = "conteudo da imagem fake".getBytes(StandardCharsets.UTF_8);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                zos.putNextEntry(new ZipEntry("conteudo.md"));
                zos.write(markdownContent.getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
                
                zos.putNextEntry(new ZipEntry("diagrama1.png"));
                zos.write(fakeImageBytes);
                zos.closeEntry();
            }
            byte[] zipBytes = baos.toByteArray();
            
            String objectKey = UUID.randomUUID().toString() + ".zip";
            
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(objectKey)
                .metadata(Map.of(
                    "description", "Analise de PDF DeepSeek",
                    "original_size", String.valueOf(pdfBytes.length)
                ))
                .build();
                
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(zipBytes));
            
            System.out.println("Processamento concluido. ZIP " + objectKey + " enviado para o S3.");
            
        } catch (Exception e) {
            System.err.println("Erro durante a analise do PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
