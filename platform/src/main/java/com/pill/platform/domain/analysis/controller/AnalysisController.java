package com.pill.platform.domain.analysis.controller;

import com.pill.platform.domain.analysis.dto.AnalysisRequest;
import com.pill.platform.domain.analysis.dto.AnalysisResponse;
import com.pill.platform.domain.analysis.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

  private final AnalysisService analysisService;

  @PostMapping
  public ResponseEntity<AnalysisResponse> analyze(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestBody(required = false) AnalysisRequest request) {
    return ResponseEntity.ok(analysisService.analyze(userDetails.getUsername(), request));
  }
}
