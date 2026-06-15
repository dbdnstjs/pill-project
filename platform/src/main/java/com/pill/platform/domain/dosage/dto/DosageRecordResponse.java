package com.pill.platform.domain.dosage.dto;

import com.pill.platform.domain.dosage.entity.DosageRecord;
import java.time.LocalDateTime;

public record DosageRecordResponse(
    Long id,
    Long userSupplementId,
    String productName,
    LocalDateTime takenAt,
    Boolean isTaken,
    String note) {

  public static DosageRecordResponse from(DosageRecord dr) {
    return new DosageRecordResponse(
        dr.getId(),
        dr.getUserSupplement().getId(),
        dr.getUserSupplement().getSupplement().getProductName(),
        dr.getTakenAt(),
        dr.getIsTaken(),
        dr.getNote());
  }
}
