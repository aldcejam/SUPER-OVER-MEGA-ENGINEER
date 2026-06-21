package com.supersys.analysis.messaging;

import com.supersys.analysis.entity.ScheduleEntity;
import com.supersys.analysis.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.concurrent.CompletableFuture;

@Service
public class ScheduleMessagingService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ScheduleRepository scheduleRepository;

    public void requestAnalysis(ScheduleEntity schedule) {
        CompletableFuture.runAsync(() -> {
            try {
                String url = "http://ai-service/api/ai/analyze-schedule";
                
                // Request analysis from ai-service and save JSON response
                String jsonResponse = restTemplate.postForObject(url, schedule, String.class);
                
                scheduleRepository.findById(schedule.getId()).ifPresent(entity -> {
                    entity.setAnalysisResult(jsonResponse);
                    entity.setStatus("ANALYZED");
                    scheduleRepository.save(entity);
                    System.out.println("Schedule " + schedule.getId() + " updated via REST with analysis result.");
                });
            } catch (Exception e) {
                System.err.println("Failed to request AI analysis via REST: " + e.getMessage());
                scheduleRepository.findById(schedule.getId()).ifPresent(entity -> {
                    entity.setStatus("ANALYSIS_FAILED");
                    scheduleRepository.save(entity);
                });
            }
        });
    }
}
