package com.supersys.ai.dto;

import java.util.List;

public record ScheduleAnalysisResponseDto(
    String status,
    String summary,
    List<String> riskPoints,
    List<String> optimizationSuggestions,
    Integer estimatedDurationDays
) {}
