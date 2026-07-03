package com.pill.platform.domain.analysis.dto;

import java.util.List;

public record NutritionSummaryResponse(
    String ageGroup, String gender, List<NutrientItem> nutrients) {
  public record NutrientItem(
      String name,
      String unit,
      double intake,
      Double recommended,
      Double upperLimit,
      int percentage,
      boolean hasAmount) {}
}
