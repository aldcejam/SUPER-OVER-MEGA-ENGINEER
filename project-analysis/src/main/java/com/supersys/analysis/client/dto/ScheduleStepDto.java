package com.supersys.analysis.client.dto;

public record ScheduleStepDto(
    String stepName,
    String description,
    Integer sequence,
    Integer daysRequired,
    Boolean completed
) {}
