package com.supersys.analysis.client;

import com.supersys.analysis.client.dto.ProjectAnalysisResponseDto;
import com.supersys.analysis.client.dto.ProjectDto;
import com.supersys.analysis.client.dto.ResourceAnalysisResponseDto;
import com.supersys.analysis.client.dto.ScheduleAnalysisResponseDto;
import com.supersys.analysis.client.dto.ScheduleDto;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/api/ai")
public interface AiServiceClient {

    @PostExchange("/analyze-schedule")
    ScheduleAnalysisResponseDto analyzeSchedule(@RequestBody ScheduleDto schedule);

    @PostExchange("/analyze-project")
    ProjectAnalysisResponseDto analyzeProject(@RequestBody ProjectDto project);

    @PostExchange("/analyze-resources")
    ResourceAnalysisResponseDto analyzeResources(@RequestBody ProjectDto project);
}
