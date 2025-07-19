package com.vslearn.dto.response;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingListResponse {
    private List<PricingDetailResponse> packages;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    
    // Statistics
    private long totalPricing;
    private long activePricing;
    private long inactivePricing;
    private long basicPricing;
    private long premiumPricing;
    private long enterprisePricing;
} 