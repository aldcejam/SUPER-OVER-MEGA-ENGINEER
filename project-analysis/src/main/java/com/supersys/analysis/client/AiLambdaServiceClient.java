package com.supersys.analysis.client;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import org.springframework.web.bind.annotation.RequestHeader;

@HttpExchange
public interface AiLambdaServiceClient {

    @PostExchange(value = "/lambdaService", contentType = "text/markdown")
    String uploadMarkdown(@RequestBody byte[] mdBytes, @RequestHeader("documentId") String documentId);
}
