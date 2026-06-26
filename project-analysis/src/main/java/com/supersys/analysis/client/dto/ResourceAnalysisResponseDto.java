package com.supersys.analysis.client.dto;

import java.util.List;

public record ResourceAnalysisResponseDto(
    String overallUtilization,
    List<String> bottlenecks,
    String costEfficiency,
    List<String> optimizationSuggestions
) {}
