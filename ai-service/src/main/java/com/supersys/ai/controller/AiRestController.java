package com.supersys.ai.controller;

import com.supersys.ai.dto.ScheduleDto;
import com.supersys.ai.dto.ScheduleAnalysisResponseDto;
import com.supersys.ai.dto.ProjectDto;
import com.supersys.ai.dto.ProjectAnalysisResponseDto;
import com.supersys.ai.dto.ResourceAnalysisResponseDto;
import com.supersys.ai.service.ScheduleAnalysisService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiRestController {

    private final ScheduleAnalysisService scheduleAnalysisService;

    @Autowired
    public AiRestController(ScheduleAnalysisService scheduleAnalysisService) {
        this.scheduleAnalysisService = scheduleAnalysisService;
    }

    @PostMapping("/analyze-schedule")
    @CircuitBreaker(name = "aiAnalysisBreaker", fallbackMethod = "analyzeScheduleFallback")
    @RateLimiter(name = "aiAnalysisLimiter")
    public ScheduleAnalysisResponseDto analyzeSchedule(@RequestBody ScheduleDto schedule) {
        return scheduleAnalysisService.analyze(schedule);
    }

    public ScheduleAnalysisResponseDto analyzeScheduleFallback(ScheduleDto schedule, Throwable t) {
        String msg;
        if (t instanceof RequestNotPermitted) {
            msg = "Rate limit exceeded. Maximum of 3 requests per 30 seconds is allowed. Please try again later.";
        } else if (t instanceof CallNotPermittedException) {
            msg = "Circuit Breaker is OPEN. AI analysis service is temporarily unavailable.";
        } else {
            msg = "AI analysis failed: " + t.getMessage();
        }
        return new ScheduleAnalysisResponseDto(
            "FAILED",
            msg,
            List.of("Service unavailable"),
            List.of("Try again later"),
            0
        );
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



