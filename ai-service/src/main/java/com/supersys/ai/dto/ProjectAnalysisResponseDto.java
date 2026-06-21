package com.supersys.ai.dto;

import java.util.List;

public record ProjectAnalysisResponseDto(
    String feasibilityScore,
    String budgetRisk,
    String feasibilitySummary,
    List<String> recommendations
) {}
