package com.pill.platform.domain.dosage.controller;

import com.pill.platform.domain.dosage.dto.AutoScheduleResponse;
import com.pill.platform.domain.dosage.dto.DosageScheduleRequest;
import com.pill.platform.domain.dosage.dto.DosageScheduleResponse;
import com.pill.platform.domain.dosage.dto.UserSupplementRequest;
import com.pill.platform.domain.dosage.dto.UserSupplementResponse;
import com.pill.platform.domain.dosage.service.UserSupplementService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user-supplements")
@RequiredArgsConstructor
public class UserSupplementController {

  private final UserSupplementService userSupplementService;

  @PostMapping
  public ResponseEntity<UserSupplementResponse> register(
      @AuthenticationPrincipal UserDetails userDetails,
      @Valid @RequestBody UserSupplementRequest request) {
    return ResponseEntity.ok(userSupplementService.register(userDetails.getUsername(), request));
  }

  @GetMapping
  public ResponseEntity<List<UserSupplementResponse>> getMySupplements(
      @AuthenticationPrincipal UserDetails userDetails) {
    return ResponseEntity.ok(userSupplementService.getMySupplements(userDetails.getUsername()));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deactivate(
      @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
    userSupplementService.deactivate(userDetails.getUsername(), id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/auto-schedule")
  public ResponseEntity<AutoScheduleResponse> autoSchedule(
      @AuthenticationPrincipal UserDetails userDetails) {
    return ResponseEntity.ok(userSupplementService.autoSchedule(userDetails.getUsername()));
  }

  @PostMapping("/{id}/schedules")
  public ResponseEntity<DosageScheduleResponse> addSchedule(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long id,
      @Valid @RequestBody DosageScheduleRequest request) {
    return ResponseEntity.ok(
        userSupplementService.addSchedule(userDetails.getUsername(), id, request));
  }

  @GetMapping("/{id}/schedules")
  public ResponseEntity<List<DosageScheduleResponse>> getSchedules(
      @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
    return ResponseEntity.ok(userSupplementService.getSchedules(userDetails.getUsername(), id));
  }

  @DeleteMapping("/{id}/schedules/{scheduleId}")
  public ResponseEntity<Void> deleteSchedule(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long id,
      @PathVariable Long scheduleId) {
    userSupplementService.deleteSchedule(userDetails.getUsername(), id, scheduleId);
    return ResponseEntity.noContent().build();
  }
}
