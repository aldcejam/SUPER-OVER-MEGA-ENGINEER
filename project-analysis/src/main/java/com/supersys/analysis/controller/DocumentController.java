package com.supersys.analysis.controller;

import com.supersys.analysis.client.AiLambdaServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private AiLambdaServiceClient lambdaClient;

    @Autowired
    private S3Client s3Client;

    private static final String BUCKET_NAME = "pdf-extractions";

    @PostMapping("/upload-pdf")
    public ResponseEntity<String> uploadPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "deepAnalysis", defaultValue = "false") boolean deepAnalysis) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Erro: O arquivo enviado esta vazio.");
        }

        try {
            byte[] pdfBytes = file.getBytes();
            String documentId = UUID.randomUUID().toString();
            String response = lambdaClient.uploadPdf(pdfBytes, deepAnalysis, documentId);
            return ResponseEntity.ok("Document ID: " + documentId + "\n" + response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro ao processar PDF: " + e.getMessage());
        }
    }

    @GetMapping("/s3-files")
    public ResponseEntity<?> listS3Files() {
        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(BUCKET_NAME)
                .build();
            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
            List<String> fileNames = listResponse.contents().stream()
                .map(S3Object::key)
                .collect(Collectors.toList());
            return ResponseEntity.ok(fileNames);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro ao listar arquivos do S3: " + e.getMessage());
        }
    }

    @GetMapping("/s3-files/download")
    public ResponseEntity<?> downloadFile(@RequestParam("filename") String filename) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(filename)
                .build();
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getRequest);
            byte[] data = objectBytes.asByteArray();

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .body(data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro ao baixar arquivo do S3: " + e.getMessage());
        }
    }
}
