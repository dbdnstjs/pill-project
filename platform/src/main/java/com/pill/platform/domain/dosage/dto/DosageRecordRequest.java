package com.pill.platform.domain.dosage.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record DosageRecordRequest(
    @NotNull Long userSupplementId,
    Long dosageScheduleId,
    @NotNull LocalDateTime takenAt,
    @NotNull Boolean isTaken,
    String note) {}
