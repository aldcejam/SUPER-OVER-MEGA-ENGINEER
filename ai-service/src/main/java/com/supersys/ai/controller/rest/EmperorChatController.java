package com.supersys.ai.controller.rest;

import com.supersys.ai.service.EmperorChatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ai/emperor")
public class EmperorChatController {

    private final EmperorChatService emperorChatService;

    public EmperorChatController(EmperorChatService emperorChatService) {
        this.emperorChatService = emperorChatService;
    }

    @GetMapping("/context")
    public Map<String, String> context() {
        return Map.of("context", emperorChatService.getContext());
    }

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> body) {
        String message = body.getOrDefault("message", "");
        return Map.of("answer", emperorChatService.chat(message));
    }
}
