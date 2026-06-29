package com.supersys.analysis.client;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import org.springframework.web.bind.annotation.RequestHeader;

@HttpExchange
public interface AiLambdaServiceClient {

    @PostExchange(value = "/lambdaService", contentType = "application/pdf")
    String uploadPdf(@RequestBody byte[] pdfBytes, @RequestHeader("deepAnalysis") boolean deepAnalysis, @RequestHeader("documentId") String documentId);
}
