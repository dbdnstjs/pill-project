package com.pill.platform.domain.analysis.dto;

import java.util.List;

public record AnalysisResponse(
    List<InteractionResult> interactions, String summary, String overallRisk) {

  public record InteractionResult(
      String ingredient1, String ingredient2, String status, String description) {}
}
