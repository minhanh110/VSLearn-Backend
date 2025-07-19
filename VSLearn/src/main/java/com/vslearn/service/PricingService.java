package com.vslearn.service;

import com.vslearn.dto.request.PricingCreateRequest;
import com.vslearn.dto.request.PricingUpdateRequest;
import com.vslearn.dto.response.PricingDetailResponse;
import com.vslearn.dto.response.PricingListResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface PricingService {
    
    // CRUD Operations
    PricingDetailResponse createPricing(PricingCreateRequest request);
    PricingDetailResponse updatePricing(Long pricingId, PricingUpdateRequest request);
    void deletePricing(Long pricingId);
    PricingDetailResponse getPricingById(Long pricingId);
    
    // List Operations
    PricingListResponse getPricingList(Pageable pageable, String search, String pricingType, Boolean isActive);
    List<PricingDetailResponse> getActivePricing();
    List<PricingDetailResponse> getPricingByType(String pricingType);
    List<PricingDetailResponse> getPricingByPriceRange(Double minPrice, Double maxPrice);
    
    // Statistics
    Map<String, Object> getPricingStats();
    
    // Status Management
    PricingDetailResponse togglePricingStatus(Long pricingId);
    
    // Export
    byte[] exportPricingToExcel(String pricingType, Boolean isActive);
} 