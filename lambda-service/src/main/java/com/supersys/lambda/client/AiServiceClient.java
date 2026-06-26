package com.supersys.lambda.client;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/api/ai")
public interface AiServiceClient {

    @PostExchange(value = "/deep-extract", contentType = "application/pdf")
    void extractPdfContent(@RequestBody byte[] pdfBytes);
}
