package com.supersys.analysis.controller;

import com.supersys.analysis.client.AiLambdaServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private AiLambdaServiceClient lambdaClient;

    @PostMapping("/upload-pdf")
    public ResponseEntity<String> uploadPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "deepAnalysis", defaultValue = "false") boolean deepAnalysis) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Erro: O arquivo enviado esta vazio.");
        }

        try {
            byte[] pdfBytes = file.getBytes();
            String response = lambdaClient.uploadPdf(pdfBytes, deepAnalysis);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro ao processar PDF: " + e.getMessage());
        }
    }
}
