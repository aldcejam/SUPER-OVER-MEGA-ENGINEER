package com.supersys.mcp.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SystemDiagnosticsTools {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    @Value("${mcp.eureka.url:http://localhost:8761/eureka/apps}")
    private String eurekaUrl;

    @Value("${mcp.config.path:config-server/src/main/resources/config}")
    private String configPath;

    @Tool(description = "Retorna o status de todos os microsserviços e instâncias registradas no Eureka Server.")
    public String getEurekaServices() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(eurekaUrl))
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                return "Falha ao obter dados do Eureka. HTTP Status: " + response.statusCode();
            }
        } catch (Exception e) {
            return "Erro ao conectar ao Eureka Server em " + eurekaUrl + ": " + e.getMessage();
        }
    }

    @Tool(description = "Lista todos os ambientes/perfis e arquivos de configuração disponíveis no diretório do Config Server.")
    public Map<String, Object> getConfigEnvironments() {
        Map<String, Object> result = new HashMap<>();
        
        File baseDir = new File(configPath);
        if (!baseDir.isAbsolute() && (!baseDir.exists() || !baseDir.isDirectory())) {
            File tryDir = new File("../" + configPath);
            if (tryDir.exists() && tryDir.isDirectory()) {
                baseDir = tryDir;
            }
        }

        if (!baseDir.exists() || !baseDir.isDirectory()) {
            result.put("erro", "Diretório de configurações não encontrado. Procurado em: " + baseDir.getAbsolutePath());
            return result;
        }

        File[] profiles = baseDir.listFiles(File::isDirectory);
        if (profiles == null || profiles.length == 0) {
            result.put("ambientesConfigurados", List.of());
            result.put("mensagem", "Nenhum ambiente encontrado no diretório.");
            return result;
        }

        List<Map<String, Object>> environmentsList = new ArrayList<>();
        for (File profileDir : profiles) {
            Map<String, Object> envInfo = new HashMap<>();
            envInfo.put("nome", profileDir.getName());
            
            File[] configFiles = profileDir.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml") || name.endsWith(".properties"));
            List<String> files = new ArrayList<>();
            if (configFiles != null) {
                for (File file : configFiles) {
                    files.add(file.getName());
                }
            }
            envInfo.put("arquivosConfiguracao", files);
            environmentsList.add(envInfo);
        }

        result.put("ambientesConfigurados", environmentsList);
        return result;
    }
}
