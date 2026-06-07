package com.pill.platform.domain.supplement.dto;

import com.pill.platform.domain.supplement.entity.Supplement;

public record SupplementResponse(
    Long id,
    String reportNo,
    String productName,
    String manufacturer,
    String shape,
    String primaryFunction,
    String caution,
    String rawMaterial) {

  public static SupplementResponse from(Supplement supplement) {
    return new SupplementResponse(
        supplement.getId(),
        supplement.getReportNo(),
        supplement.getProductName(),
        supplement.getManufacturer(),
        supplement.getShape(),
        supplement.getPrimaryFunction(),
        supplement.getCaution(),
        supplement.getRawMaterial());
  }
}
