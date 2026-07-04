package com.supersys.ai.controller.rest;

import com.supersys.ai.service.AiQueryService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiRestController {

    private final AiQueryService aiQueryService;

    public AiRestController(AiQueryService aiQueryService) {
        this.aiQueryService = aiQueryService;
    }

    @PostMapping("/query")
    @CircuitBreaker(name = "aiAnalysisBreaker", fallbackMethod = "queryFallback")
    public String query(@RequestBody Map<String, String> body) {
        String query = body.get("query");
        if (query == null || query.isBlank()) {
            return "Please provide a query.";
        }
        return aiQueryService.askDeepSeek(query);
    }

    public String queryFallback(Map<String, String> body, Throwable t) {
        return "Desculpe, o servico de IA esta temporariamente indisponivel (Fallback ativado). Erro: " + t.getMessage();
    }
}
