package com.pill.platform.domain.supplement.dto;

public record SupplementSearchResult(
    String reportNo,
    String productName,
    String manufacturer,
    String shape,
    String primaryFunction,
    String caution,
    String rawMaterial) {}
