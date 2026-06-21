package com.supersys.ai.controller;

import com.supersys.ai.dto.ScheduleDto;
import com.supersys.ai.dto.ScheduleAnalysisResponseDto;
import com.supersys.ai.dto.ProjectDto;
import com.supersys.ai.dto.ProjectAnalysisResponseDto;
import com.supersys.ai.dto.ResourceAnalysisResponseDto;
import com.supersys.ai.service.ScheduleAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiRestController {

    private final ScheduleAnalysisService scheduleAnalysisService;

    @Autowired
    public AiRestController(ScheduleAnalysisService scheduleAnalysisService) {
        this.scheduleAnalysisService = scheduleAnalysisService;
    }

    @PostMapping("/analyze-schedule")
    public ScheduleAnalysisResponseDto analyzeSchedule(@RequestBody ScheduleDto schedule) {
        return scheduleAnalysisService.analyze(schedule);
    }

    @PostMapping("/analyze-project")
    public ProjectAnalysisResponseDto analyzeProject(@RequestBody ProjectDto project) {
        return scheduleAnalysisService.analyzeProject(project);
    }

    @PostMapping("/analyze-resources")
    public ResourceAnalysisResponseDto analyzeResources(@RequestBody ProjectDto project) {
        return scheduleAnalysisService.analyzeResources(project);
    }
}


