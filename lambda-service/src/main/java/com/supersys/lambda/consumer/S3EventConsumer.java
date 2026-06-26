package com.supersys.lambda.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.nio.charset.StandardCharsets;

@Service
public class S3EventConsumer {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private VectorStore vectorStore;

    @SqsListener("pdf-extraction-events")
    public void receiveEvent(String message) {
        System.out.println("Evento SQS recebido: " + message);
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(message);
            
            if (rootNode.has("Records")) {
                for (JsonNode record : rootNode.get("Records")) {
                    if (record.has("eventSource") && "aws:s3".equals(record.get("eventSource").asText())) {
                        String bucketName = record.get("s3").get("bucket").get("name").asText();
                        String objectKey = record.get("s3").get("object").get("key").asText();
                        
                        processZipFromS3(bucketName, objectKey);
                    }
                }
            } else if (rootNode.has("Message")) {
                JsonNode snsMessage = mapper.readTree(rootNode.get("Message").asText());
                if (snsMessage.has("Records")) {
                    for (JsonNode record : snsMessage.get("Records")) {
                        String bucketName = record.get("s3").get("bucket").get("name").asText();
                        String objectKey = record.get("s3").get("object").get("key").asText();
                        processZipFromS3(bucketName, objectKey);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar evento SQS: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processZipFromS3(String bucketName, String objectKey) {
        System.out.println("Baixando arquivo do S3: " + bucketName + "/" + objectKey);
        
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
            HeadObjectResponse headResponse = s3Client.headObject(headRequest);
            Map<String, String> metadata = headResponse.metadata(); 
            
            GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
            
            try (InputStream s3InputStream = s3Client.getObject(getRequest);
                 ZipInputStream zis = new ZipInputStream(s3InputStream)) {
                
                ZipEntry entry;
                String markdownContent = null;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().endsWith(".md")) {
                        byte[] bytes = zis.readAllBytes();
                        markdownContent = new String(bytes, StandardCharsets.UTF_8);
                        break;
                    }
                    zis.closeEntry();
                }
                
                if (markdownContent != null) {
                    String docId = objectKey.replace(".zip", "");
                    Document aiDoc = new Document(
                        docId,
                        markdownContent,
                        Map.of(
                            "source", "s3-deep-analysis",
                            "bucket", bucketName,
                            "key", objectKey,
                            "timestamp", System.currentTimeMillis(),
                            "description", metadata.getOrDefault("description", "No description provided")
                        )
                    );
                    
                    vectorStore.add(List.of(aiDoc));
                    System.out.println("Documento " + docId + " extraido do ZIP e inserido no pgvector com sucesso!");
                } else {
                    System.err.println("Nenhum arquivo .md encontrado no ZIP " + objectKey);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao baixar ou processar ZIP do S3: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
