package com.pill.platform.domain.dosage.dto;

import java.time.LocalTime;
import java.util.List;

public record TodayChecklistResponse(List<Item> items, String caution) {

  public record Item(
      Long dosageScheduleId,
      Long userSupplementId,
      String productName,
      LocalTime scheduledTime,
      String bucketLabel,
      boolean isTaken) {}
}
