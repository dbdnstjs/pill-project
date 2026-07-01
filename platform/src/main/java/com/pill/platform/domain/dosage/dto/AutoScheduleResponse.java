package com.pill.platform.domain.dosage.dto;

import java.time.LocalTime;
import java.util.List;

public record AutoScheduleResponse(List<Item> items, String caution) {

  public record Item(
      Long userSupplementId, String productName, String bucketLabel, LocalTime scheduledTime) {}
}
