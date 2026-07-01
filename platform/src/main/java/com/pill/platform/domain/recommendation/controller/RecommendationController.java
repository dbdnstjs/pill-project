package com.pill.platform.domain.recommendation.controller;

import com.pill.platform.domain.recommendation.dto.RecommendationResponse;
import com.pill.platform.domain.recommendation.entity.Symptom;
import com.pill.platform.domain.recommendation.service.SymptomRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

  private final SymptomRecommendationService symptomRecommendationService;

  @GetMapping
  public ResponseEntity<RecommendationResponse> recommend(
      @AuthenticationPrincipal UserDetails userDetails, @RequestParam Symptom symptom) {
    return ResponseEntity.ok(
        symptomRecommendationService.recommend(userDetails.getUsername(), symptom));
  }
}
