package com.supersys.ai.messaging;

import com.supersys.ai.dto.ScheduleDto;
import com.supersys.ai.dto.ScheduleAnalysisResponseDto;
import com.supersys.ai.service.ScheduleAnalysisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class ScheduleAnalysisListener {

    public static final String REQUEST_QUEUE = "schedule-analysis-request-queue";
    public static final String RESPONSE_QUEUE = "schedule-analysis-response-queue";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ScheduleAnalysisService scheduleAnalysisService;

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = REQUEST_QUEUE)
    public void handleScheduleAnalysisRequest(String requestPayload) {
        // Expected payload: "ID:123|TITLE:Build A|DETAILS:Phase 1..."
        if (requestPayload == null || !requestPayload.startsWith("ID:")) return;

        try {
            int separatorIndex = requestPayload.indexOf("|TITLE:");
            if (separatorIndex == -1) return;

            Long scheduleId = Long.parseLong(requestPayload.substring(3, separatorIndex));
            
            int detailsIndex = requestPayload.indexOf("|DETAILS:");
            String title = "";
            String details = "";
            if (detailsIndex != -1) {
                title = requestPayload.substring(separatorIndex + 7, detailsIndex);
                details = requestPayload.substring(detailsIndex + 9);
            } else {
                title = requestPayload.substring(separatorIndex + 7);
            }

            // Construct standard DTO
            ScheduleDto scheduleDto = new ScheduleDto(
                scheduleId,
                title,
                null,
                null,
                null,
                details,
                Collections.emptyList()
            );

            // Delegate to the unified service
            ScheduleAnalysisResponseDto responseDto = scheduleAnalysisService.analyze(scheduleDto);
            String rawJsonResult = objectMapper.writeValueAsString(responseDto);
            
            // Send back the standardized JSON response
            String responsePayload = String.format("ID:%d|RESULT:%s", scheduleId, rawJsonResult);
            rabbitTemplate.convertAndSend(RESPONSE_QUEUE, responsePayload);
            System.out.println("AI Service responded to Schedule " + scheduleId + " with JSON analysis.");

        } catch (Exception e) {
            System.err.println("Error analyzing schedule: " + e.getMessage());
        }
    }
}


