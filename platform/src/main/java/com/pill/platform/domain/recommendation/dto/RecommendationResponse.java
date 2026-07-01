package com.pill.platform.domain.recommendation.dto;

import java.util.List;

public record RecommendationResponse(String symptom, String symptomLabel, List<Item> items) {

  public record Item(String ingredientName, String reason, boolean alreadySufficient) {}
}
