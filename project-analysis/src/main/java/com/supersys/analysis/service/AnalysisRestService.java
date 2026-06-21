package com.supersys.analysis.service;

import com.supersys.analysis.entity.ProjectEntity;
import com.supersys.analysis.entity.ScheduleEntity;
import com.supersys.analysis.repository.ProjectRepository;
import com.supersys.analysis.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.concurrent.CompletableFuture;

@Service
public class AnalysisRestService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private ProjectRepository projectRepository;

    public void requestScheduleAnalysis(ScheduleEntity schedule) {
        CompletableFuture.runAsync(() -> {
            try {
                String url = "http://ai-service/api/ai/analyze-schedule";
                String jsonResponse = restTemplate.postForObject(url, schedule, String.class);
                
                scheduleRepository.findById(schedule.getId()).ifPresent(entity -> {
                    entity.setAnalysisResult(jsonResponse);
                    entity.setStatus("ANALYZED");
                    scheduleRepository.save(entity);
                    System.out.println("Schedule " + schedule.getId() + " updated via REST.");
                });
            } catch (Exception e) {
                System.err.println("Failed to request schedule analysis: " + e.getMessage());
                scheduleRepository.findById(schedule.getId()).ifPresent(entity -> {
                    entity.setStatus("ANALYSIS_FAILED");
                    scheduleRepository.save(entity);
                });
            }
        });
    }

    public void requestProjectAnalysis(ProjectEntity project) {
        CompletableFuture.runAsync(() -> {
            try {
                String url = "http://ai-service/api/ai/analyze-project";
                String jsonResponse = restTemplate.postForObject(url, project, String.class);
                
                projectRepository.findById(project.getId()).ifPresent(entity -> {
                    entity.setAnalysisResult(jsonResponse);
                    entity.setStatus("ANALYZED");
                    projectRepository.save(entity);
                    System.out.println("Project " + project.getId() + " updated via REST.");
                });
            } catch (Exception e) {
                System.err.println("Failed to request project analysis: " + e.getMessage());
                projectRepository.findById(project.getId()).ifPresent(entity -> {
                    entity.setStatus("ANALYSIS_FAILED");
                    projectRepository.save(entity);
                });
            }
        });
    }

    public void requestResourceAnalysis(ProjectEntity project) {
        CompletableFuture.runAsync(() -> {
            try {
                String url = "http://ai-service/api/ai/analyze-resources";
                String jsonResponse = restTemplate.postForObject(url, project, String.class);
                
                projectRepository.findById(project.getId()).ifPresent(entity -> {
                    entity.setResourceAnalysisResult(jsonResponse);
                    entity.setStatus("RESOURCES_ANALYZED");
                    projectRepository.save(entity);
                    System.out.println("Project resource allocation " + project.getId() + " updated via REST.");
                });
            } catch (Exception e) {
                System.err.println("Failed to request resource analysis: " + e.getMessage());
                projectRepository.findById(project.getId()).ifPresent(entity -> {
                    entity.setStatus("RESOURCES_ANALYSIS_FAILED");
                    projectRepository.save(entity);
                });
            }
        });
    }
}
