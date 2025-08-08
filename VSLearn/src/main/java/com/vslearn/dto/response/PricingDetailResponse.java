package com.vslearn.dto.response;

import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingDetailResponse {
    private Long id;
    private String pricingType;

    private String description;
    private Long price;
    private Long durationDays;

    private Double discountPercent;
    private Boolean isActive;
    private Instant createdAt;
    private Long createdBy;
    private Instant updatedAt;
    private Long updatedBy;
} 