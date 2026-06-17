package com.supersys.empire.messaging;

import com.supersys.empire.entity.ScheduleEntity;
import com.supersys.empire.repository.ScheduleRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduleMessagingService {

    public static final String REQUEST_QUEUE = "schedule-analysis-request-queue";
    public static final String RESPONSE_QUEUE = "schedule-analysis-response-queue";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ScheduleRepository scheduleRepository;

    public void requestAnalysis(ScheduleEntity schedule) {
        String messagePayload = String.format("ID:%d|TITLE:%s|DETAILS:%s", 
            schedule.getId(), schedule.getTitle(), schedule.getDetails());
        rabbitTemplate.convertAndSend(REQUEST_QUEUE, messagePayload);
    }

    @RabbitListener(queues = RESPONSE_QUEUE)
    @Transactional
    public void receiveAnalysisResponse(String responsePayload) {
        if (responsePayload == null || !responsePayload.startsWith("ID:")) return;
        
        try {
            int separatorIndex = responsePayload.indexOf("|RESULT:");
            if (separatorIndex == -1) return;

            Long scheduleId = Long.parseLong(responsePayload.substring(3, separatorIndex));
            String analysisText = responsePayload.substring(separatorIndex + 8);

            scheduleRepository.findById(scheduleId).ifPresent(schedule -> {
                schedule.setAnalysisResult(analysisText);
                schedule.setStatus("ANALYZED");
                scheduleRepository.save(schedule);
                System.out.println("Schedule " + scheduleId + " updated with analysis result.");
            });
        } catch (Exception e) {
            System.err.println("Failed to process analysis response: " + e.getMessage());
        }
    }
}
