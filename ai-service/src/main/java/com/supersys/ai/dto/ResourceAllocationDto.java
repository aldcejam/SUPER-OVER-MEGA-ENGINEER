package com.supersys.ai.dto;

public record ResourceAllocationDto(
    Long id,
    String resourceName,
    String role,
    Integer hoursPerWeek,
    Double costPerHour,
    Integer quantity
) {}
