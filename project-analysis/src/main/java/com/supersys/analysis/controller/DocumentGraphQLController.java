package com.supersys.analysis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DocumentGraphQLController {

    @Autowired
    private S3Client s3Client;

    private static final String BUCKET_NAME = "pdf-extractions";

    @QueryMapping
    public List<String> listS3Files() {
        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(BUCKET_NAME)
                .build();
            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
            return listResponse.contents().stream()
                .map(S3Object::key)
                .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao listar arquivos do S3: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public String downloadS3File(@Argument String filename) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(filename)
                .build();
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getRequest);
            return objectBytes.asUtf8String();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao carregar arquivo do S3: " + e.getMessage(), e);
        }
    }
}
