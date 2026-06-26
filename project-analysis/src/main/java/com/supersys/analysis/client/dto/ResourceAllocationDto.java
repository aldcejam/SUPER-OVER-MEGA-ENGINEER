package com.supersys.analysis.client.dto;

public record ResourceAllocationDto(
    Long id,
    String resourceName,
    String role,
    Integer hoursPerWeek,
    Double costPerHour,
    Integer quantity
) {}
