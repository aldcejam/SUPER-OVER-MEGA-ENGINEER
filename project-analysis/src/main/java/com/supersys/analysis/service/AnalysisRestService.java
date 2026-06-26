package com.supersys.analysis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supersys.analysis.client.AiServiceClient;
import com.supersys.analysis.client.AiLambdaServiceClient;
import com.supersys.analysis.client.dto.*;
import com.supersys.analysis.entity.ProjectEntity;
import com.supersys.analysis.entity.ScheduleEntity;
import com.supersys.analysis.repository.ProjectRepository;
import com.supersys.analysis.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class AnalysisRestService {

    @Autowired
    private AiServiceClient aiServiceClient;

    @Autowired
    private AiLambdaServiceClient aiLambdaServiceClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private ScheduleDto mapSchedule(ScheduleEntity entity) {
        if (entity == null) return null;
        List<ScheduleStepDto> steps = null;
        if (entity.getSteps() != null) {
            steps = entity.getSteps().stream()
                .map(s -> new ScheduleStepDto(s.getStepName(), s.getDescription(), s.getSequence(), s.getDaysRequired(), s.getCompleted()))
                .toList();
        }
        return new ScheduleDto(entity.getId(), entity.getTitle(), entity.getStartDate(), entity.getEndDate(), entity.getStatus(), entity.getDetails(), steps);
    }

    private ProjectDto mapProject(ProjectEntity entity) {
        if (entity == null) return null;
        ScheduleDto scheduleDto = mapSchedule(entity.getSchedule());
        List<ResourceAllocationDto> allocs = null;
        if (entity.getAllocations() != null) {
            allocs = entity.getAllocations().stream()
                .map(a -> new ResourceAllocationDto(a.getId(), a.getResourceName(), a.getRole(), a.getHoursPerWeek(), a.getCostPerHour(), a.getQuantity()))
                .toList();
        }
        return new ProjectDto(entity.getId(), entity.getName(), entity.getDescription(), entity.getBudget(), entity.getStatus(), scheduleDto, allocs);
    }

    public void requestScheduleAnalysis(ScheduleEntity schedule) {
        CompletableFuture.runAsync(() -> {
            try {
                ScheduleDto requestDto = mapSchedule(schedule);
                ScheduleAnalysisResponseDto responseDto = aiServiceClient.analyzeSchedule(requestDto);
                String jsonResponse = objectMapper.writeValueAsString(responseDto);
                
                scheduleRepository.findById(schedule.getId()).ifPresent(entity -> {
                    entity.setAnalysisResult(jsonResponse);
                    entity.setStatus("ANALYZED");
                    scheduleRepository.save(entity);
                    System.out.println("Schedule " + schedule.getId() + " updated via HTTP Interface.");
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
                ProjectDto requestDto = mapProject(project);
                ProjectAnalysisResponseDto responseDto = aiServiceClient.analyzeProject(requestDto);
                String jsonResponse = objectMapper.writeValueAsString(responseDto);
                
                projectRepository.findById(project.getId()).ifPresent(entity -> {
                    entity.setAnalysisResult(jsonResponse);
                    entity.setStatus("ANALYZED");
                    projectRepository.save(entity);
                    System.out.println("Project " + project.getId() + " updated via HTTP Interface.");
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
                ProjectDto requestDto = mapProject(project);
                ResourceAnalysisResponseDto responseDto = aiServiceClient.analyzeResources(requestDto);
                String jsonResponse = objectMapper.writeValueAsString(responseDto);
                
                projectRepository.findById(project.getId()).ifPresent(entity -> {
                    entity.setResourceAnalysisResult(jsonResponse);
                    entity.setStatus("RESOURCES_ANALYZED");
                    projectRepository.save(entity);
                    System.out.println("Project resource allocation " + project.getId() + " updated via HTTP Interface.");
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
