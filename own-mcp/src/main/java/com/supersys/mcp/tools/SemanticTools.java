package com.supersys.mcp.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SemanticTools {

    private static final List<Map<String, String>> KB = List.of(
        Map.of(
            "title", "Arquitetura Resiliente SUPER-SYS",
            "content", "O sistema SUPER-SYS utiliza Spring Cloud Gateway na porta 8080 para roteamento reativo. Os microsserviços estão registrados no Eureka Server (porta 8761). As configurações são gerenciadas de forma centralizada pelo Config Server (porta 8888)."
        ),
        Map.of(
            "title", "Configurações Resilience4j",
            "content", "O microserviço Coordinator implementa disjuntor (Circuit Breaker) com sliding window de 10 chamadas e taxa de erro limite de 50%, permitindo autorrecuperação e falha controlada (fallback)."
        ),
        Map.of(
            "title", "Inteligência Artificial & Gemini",
            "content", "O ai-service integra a API do Google Gemini via Spring AI e utiliza PostgreSQL com pgvector para busca semântica em tempo de execução, alimentando contextos no fluxo RAG."
        )
    );

    @Tool(description = "Busca informações técnicas em nossa base de conhecimento interna.")
    public List<Map<String, String>> searchKb(
            @ToolParam(description = "O termo ou pergunta de pesquisa.") String query) {
        
        List<Map<String, String>> filtered = KB.stream()
                .filter(doc -> doc.get("title").toLowerCase().contains(query.toLowerCase()) ||
                               doc.get("content").toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            return List.of(Map.of("title", "Sem resultados", "content", "Nenhum documento encontrado na base de conhecimento local."));
        }
        return filtered;
    }

    @Tool(description = "Calcula métricas de desempenho de requisições e taxa de falhas.")
    public Map<String, Object> calculateMetrics(
            @ToolParam(description = "Total de requisições recebidas.") double requestCount,
            @ToolParam(description = "Total de falhas registradas.") double failureCount) {

        double errorRate = requestCount > 0 ? (failureCount / requestCount) * 100 : 0;
        double reliability = 100 - errorRate;
        String status = errorRate > 50 ? "CRITICAL (Circuit Breaker OPEN)" : "HEALTHY";

        return Map.of(
                "requestCount", requestCount,
                "failureCount", failureCount,
                "errorRate", String.format("%.2f%%", errorRate),
                "reliability", String.format("%.2f%%", reliability),
                "status", status
        );
    }
}
