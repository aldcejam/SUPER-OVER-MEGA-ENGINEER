package com.supersys.analysis.controller.rest;

import com.supersys.analysis.client.AiLambdaServiceClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
public class DocumentRestController {

    private final AiLambdaServiceClient aiLambdaServiceClient;

    public DocumentRestController(AiLambdaServiceClient aiLambdaServiceClient) {
        this.aiLambdaServiceClient = aiLambdaServiceClient;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".md")) {
            return ResponseEntity.badRequest().body("Please upload a valid .md file");
        }

        try {
            String documentId = UUID.randomUUID().toString();
            String response = aiLambdaServiceClient.uploadMarkdown(file.getBytes(), documentId);
            return ResponseEntity.ok("Document forwarded to lambda successfully. Response: " + response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error uploading document: " + e.getMessage());
        }
    }
}
