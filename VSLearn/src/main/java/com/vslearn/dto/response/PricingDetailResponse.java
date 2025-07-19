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
    private String packageName;
    private String description;
    private Long price;
    private Long durationDays;
    private Integer maxVocabCount;
    private Integer maxTestCount;
    private Boolean isActive;
    private Double discountPercent;
    private Instant createdAt;
    private Long createdBy;
    private Instant updatedAt;
    private Long updatedBy;
} 