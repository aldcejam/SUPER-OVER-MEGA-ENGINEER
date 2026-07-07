package com.supersys.ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

@Service
@RefreshScope
public class EmperorChatService {

    private final ChatClient chatClient;

    @Value("${supersys.ai.prompt.emperor-context}")
    private String emperorContext;

    public EmperorChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String getContext() {
        return emperorContext;
    }

    public String chat(String message) {
        return chatClient.prompt()
                .system(emperorContext)
                .user(message)
                .call()
                .content();
    }
}
