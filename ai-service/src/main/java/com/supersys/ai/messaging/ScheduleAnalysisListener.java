package com.supersys.ai.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScheduleAnalysisListener {

    public static final String REQUEST_QUEUE = "schedule-analysis-request-queue";
    public static final String RESPONSE_QUEUE = "schedule-analysis-response-queue";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = REQUEST_QUEUE)
    public void handleScheduleAnalysisRequest(String requestPayload) {
        // Expected payload: "ID:123|TITLE:Build A|DETAILS:Phase 1..."
        if (requestPayload == null || !requestPayload.startsWith("ID:")) return;

        try {
            int separatorIndex = requestPayload.indexOf("|TITLE:");
            if (separatorIndex == -1) return;

            Long scheduleId = Long.parseLong(requestPayload.substring(3, separatorIndex));
            
            // Mock AI processing
            String analysisResult = "Análise concluída com sucesso via AI-Service. O cronograma parece bem distribuído.";
            
            // Send back
            String responsePayload = String.format("ID:%d|RESULT:%s", scheduleId, analysisResult);
            rabbitTemplate.convertAndSend(RESPONSE_QUEUE, responsePayload);
            System.out.println("AI Service responded to Schedule " + scheduleId);

        } catch (Exception e) {
            System.err.println("Error analyzing schedule: " + e.getMessage());
        }
    }
}
