package com.supersys.mcp.service;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DatabaseMcpService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @McpTool(description = "Executa uma consulta SQL SELECT de leitura de forma segura no banco de dados relacional. Retorna a lista de registros.")
    public List<Map<String, Object>> executeSelectQuery(
            @McpToolParam(description = "A consulta SQL SELECT completa (ex: SELECT * FROM projects)", required = true) String sql) {
        
        String trimmedSql = sql.trim().toLowerCase();
        if (!trimmedSql.startsWith("select")) {
            throw new IllegalArgumentException("Apenas consultas SELECT de leitura sao permitidas por motivos de seguranca.");
        }
        
        // Bloquear possiveis escritas/injecoes perigosas adicionais
        if (trimmedSql.contains("insert") || trimmedSql.contains("update") || 
            trimmedSql.contains("delete") || trimmedSql.contains("drop") || 
            trimmedSql.contains("alter") || trimmedSql.contains("create") || 
            trimmedSql.contains("truncate") || trimmedSql.contains("grant") ||
            trimmedSql.contains(";") || trimmedSql.contains("--")) {
            throw new IllegalArgumentException("Apenas consultas de leitura simples sao permitidas. Comandos de alteracao/escrita ou multiplas instrucoes nao sao permitidos.");
        }
        
        return jdbcTemplate.queryForList(sql);
    }
}
