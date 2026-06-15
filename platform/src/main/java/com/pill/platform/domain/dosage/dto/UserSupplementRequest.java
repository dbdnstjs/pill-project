package com.pill.platform.domain.dosage.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UserSupplementRequest(@NotNull Long supplementId, LocalDate startDate) {}
