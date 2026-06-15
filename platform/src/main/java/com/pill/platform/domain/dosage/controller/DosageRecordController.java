package com.pill.platform.domain.dosage.controller;

import com.pill.platform.domain.dosage.dto.DosageRecordRequest;
import com.pill.platform.domain.dosage.dto.DosageRecordResponse;
import com.pill.platform.domain.dosage.service.DosageRecordService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dosage-records")
@RequiredArgsConstructor
public class DosageRecordController {

  private final DosageRecordService dosageRecordService;

  @PostMapping
  public ResponseEntity<DosageRecordResponse> record(
      @AuthenticationPrincipal UserDetails userDetails,
      @Valid @RequestBody DosageRecordRequest request) {
    return ResponseEntity.ok(dosageRecordService.record(userDetails.getUsername(), request));
  }

  @GetMapping
  public ResponseEntity<List<DosageRecordResponse>> getByDate(
      @AuthenticationPrincipal UserDetails userDetails, @RequestParam LocalDate date) {
    return ResponseEntity.ok(dosageRecordService.getByDate(userDetails.getUsername(), date));
  }
}
