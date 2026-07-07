package com.supersys.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Autowired
    private ObjectMapper objectMapper;

    @McpTool(description = "Executa uma consulta SQL SELECT de leitura de forma segura no banco de dados relacional. Retorna os registros encontrados em formato JSON.")
    public String executeSelectQuery(
            @McpToolParam(description = "A consulta SQL SELECT completa (ex: SELECT * FROM project)", required = true) String sql) {

        String trimmedSql = sql.trim().toLowerCase();
        if (!trimmedSql.startsWith("select")) {
            throw new IllegalArgumentException("Apenas consultas SELECT de leitura sao permitidas por motivos de seguranca.");
        }

        if (trimmedSql.contains("insert") || trimmedSql.contains("update") ||
            trimmedSql.contains("delete") || trimmedSql.contains("drop") ||
            trimmedSql.contains("alter") || trimmedSql.contains("create") ||
            trimmedSql.contains("truncate") || trimmedSql.contains("grant") ||
            trimmedSql.contains(";") || trimmedSql.contains("--")) {
            throw new IllegalArgumentException("Apenas consultas de leitura simples sao permitidas. Comandos de alteracao/escrita ou multiplas instrucoes nao sao permitidos.");
        }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        try {
            return objectMapper.writeValueAsString(rows);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao serializar o resultado da consulta: " + e.getMessage(), e);
        }
    }
}
