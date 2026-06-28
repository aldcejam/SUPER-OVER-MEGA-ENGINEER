package com.supersys.ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/github")
public class GitHubAnalysisController {

    private final ChatClient chatClient;

    public GitHubAnalysisController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/resumo")
    public String resumoRepo(@RequestParam(defaultValue = "SeuUsuario/SUPER-SYS") String repository) {
        return chatClient.prompt()
            .user("Por favor, use as ferramentas MCP do GitHub para analisar a estrutura e os últimos commits do repositório '" + repository + "'. Faça um resumo conciso das entregas recentes e arquivos principais.")
            .call()
            .content();
    }
}
