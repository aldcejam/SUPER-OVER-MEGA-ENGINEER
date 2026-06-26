package com.supersys.analysis.client;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import org.springframework.web.bind.annotation.RequestHeader;

@HttpExchange
public interface AiLambdaServiceClient {

    @PostExchange("/lambdaService")
    String uploadPdf(@RequestBody byte[] pdfBytes, @RequestHeader("deepAnalysis") boolean deepAnalysis);
}
