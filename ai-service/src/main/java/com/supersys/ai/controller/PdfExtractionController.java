package com.supersys.ai.controller;

import com.supersys.ai.service.DeepSeekExtractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class PdfExtractionController {

    @Autowired
    private DeepSeekExtractionService extractionService;

    @PostMapping("/deep-extract")
    public ResponseEntity<Void> extractPdfContent(@RequestBody byte[] pdfBytes) {
        extractionService.processPdf(pdfBytes);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
