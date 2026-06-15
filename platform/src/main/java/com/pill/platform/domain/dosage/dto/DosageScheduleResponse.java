package com.pill.platform.domain.dosage.dto;

import com.pill.platform.domain.dosage.entity.DosageSchedule;
import java.time.LocalTime;

public record DosageScheduleResponse(
    Long id,
    LocalTime scheduledTime,
    Boolean monday,
    Boolean tuesday,
    Boolean wednesday,
    Boolean thursday,
    Boolean friday,
    Boolean saturday,
    Boolean sunday,
    Boolean isActive) {

  public static DosageScheduleResponse from(DosageSchedule ds) {
    return new DosageScheduleResponse(
        ds.getId(),
        ds.getScheduledTime(),
        ds.getMonday(),
        ds.getTuesday(),
        ds.getWednesday(),
        ds.getThursday(),
        ds.getFriday(),
        ds.getSaturday(),
        ds.getSunday(),
        ds.getIsActive());
  }
}
