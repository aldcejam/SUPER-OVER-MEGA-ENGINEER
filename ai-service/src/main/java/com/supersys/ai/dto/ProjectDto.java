package com.supersys.ai.dto;

import java.util.List;

public record ProjectDto(
    Long id,
    String name,
    String description,
    Double budget,
    String status,
    ScheduleDto schedule,
    List<ResourceAllocationDto> allocations
) {}
