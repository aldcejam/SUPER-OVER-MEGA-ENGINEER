package com.supersys.ai.dto;

public record ScheduleStepDto(
    String stepName,
    String description,
    Integer sequence,
    Integer daysRequired,
    Boolean completed
) {}
