package com.supersys.ai.dto;

import java.time.LocalDate;
import java.util.List;

public record ScheduleDto(
    Long id,
    String title,
    LocalDate startDate,
    LocalDate endDate,
    String status,
    String details,
    List<ScheduleStepDto> steps
) {}
