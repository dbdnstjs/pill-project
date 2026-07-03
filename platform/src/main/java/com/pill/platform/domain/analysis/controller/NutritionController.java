package com.pill.platform.domain.analysis.controller;

import com.pill.platform.domain.analysis.dto.NutritionSummaryResponse;
import com.pill.platform.domain.analysis.service.NutritionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nutrition")
@RequiredArgsConstructor
public class NutritionController {

  private final NutritionService nutritionService;

  @GetMapping("/summary")
  public ResponseEntity<NutritionSummaryResponse> getSummary(
      @AuthenticationPrincipal UserDetails userDetails) {
    return ResponseEntity.ok(nutritionService.getSummary(userDetails.getUsername()));
  }

  record UpdateAmountRequest(Long supplementId, String ingredientName, double amount, String unit) {}

  @PutMapping("/ingredient-amount")
  public ResponseEntity<Void> updateIngredientAmount(
      @RequestBody UpdateAmountRequest req) {
    nutritionService.updateIngredientAmount(
        req.supplementId(), req.ingredientName(), req.amount(), req.unit());
    return ResponseEntity.ok().build();
  }
}
