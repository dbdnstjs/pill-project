package com.pill.platform.domain.dosage.dto;

import com.pill.platform.domain.dosage.entity.UserSupplement;
import java.time.LocalDate;

public record UserSupplementResponse(
    Long id,
    Long supplementId,
    String productName,
    String manufacturer,
    LocalDate startDate,
    LocalDate endDate,
    Boolean isActive) {

  public static UserSupplementResponse from(UserSupplement us) {
    return new UserSupplementResponse(
        us.getId(),
        us.getSupplement().getId(),
        us.getSupplement().getProductName(),
        us.getSupplement().getManufacturer(),
        us.getStartDate(),
        us.getEndDate(),
        us.getIsActive());
  }
}
