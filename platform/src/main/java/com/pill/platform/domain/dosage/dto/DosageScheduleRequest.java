package com.pill.platform.domain.dosage.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record DosageScheduleRequest(
    @NotNull LocalTime scheduledTime,
    Boolean monday,
    Boolean tuesday,
    Boolean wednesday,
    Boolean thursday,
    Boolean friday,
    Boolean saturday,
    Boolean sunday) {}
