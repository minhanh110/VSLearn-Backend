package com.vslearn.service.impl;

import com.vslearn.dto.request.PricingCreateRequest;
import com.vslearn.dto.request.PricingUpdateRequest;
import com.vslearn.dto.response.PricingDetailResponse;
import com.vslearn.dto.response.PricingListResponse;
import com.vslearn.entities.Pricing;
import com.vslearn.repository.PricingRepository;
import com.vslearn.service.PricingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PricingServiceImpl implements PricingService {
    
    private final PricingRepository pricingRepository;
    
    @Autowired
    public PricingServiceImpl(PricingRepository pricingRepository) {
        this.pricingRepository = pricingRepository;
    }
    
    @Override
    public PricingDetailResponse createPricing(PricingCreateRequest request) {
        Pricing pricing = Pricing.builder()
                .pricingType(request.getPricingType())
                .description(request.getDescription())
                .price(request.getPrice())
                .durationDays(request.getDurationDays())
                .discountPercent(request.getDiscountPercent())
                .isActive(request.getIsActive())
                .createdAt(Instant.now())
                .createdBy(1L) // TODO: Get from current user context
                .build();
        
        Pricing savedPricing = pricingRepository.save(pricing);
        return convertToResponse(savedPricing);
    }
    
    @Override
    public PricingDetailResponse updatePricing(Long pricingId, PricingUpdateRequest request) {
        Optional<Pricing> optionalPricing = pricingRepository.findById(pricingId);
        if (optionalPricing.isEmpty()) {
            throw new RuntimeException("Pricing not found with id: " + pricingId);
        }
        
        Pricing pricing = optionalPricing.get();
        
        if (request.getPricingType() != null) {
            pricing.setPricingType(request.getPricingType());
        }

        if (request.getDescription() != null) {
            pricing.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            pricing.setPrice(request.getPrice());
        }
        if (request.getDurationDays() != null) {
            pricing.setDurationDays(request.getDurationDays());
        }


        if (request.getDiscountPercent() != null) {
            pricing.setDiscountPercent(request.getDiscountPercent());
        }
        
        if (request.getIsActive() != null) {
            pricing.setIsActive(request.getIsActive());
        }
        
        pricing.setUpdatedAt(Instant.now());
        pricing.setUpdatedBy(1L); // TODO: Get from current user context
        
        Pricing updatedPricing = pricingRepository.save(pricing);
        return convertToResponse(updatedPricing);
    }
    
    @Override
    public void deletePricing(Long pricingId) {
        Optional<Pricing> optionalPricing = pricingRepository.findById(pricingId);
        if (optionalPricing.isEmpty()) {
            throw new RuntimeException("Pricing not found with id: " + pricingId);
        }
        
        Pricing pricing = optionalPricing.get();
        pricing.setDeletedAt(Instant.now());
        pricing.setDeletedBy(1L); // TODO: Get from current user context

        
        pricingRepository.save(pricing);
    }
    
    @Override
    public PricingDetailResponse getPricingById(Long pricingId) {
        Optional<Pricing> optionalPricing = pricingRepository.findById(pricingId);
        if (optionalPricing.isEmpty()) {
            throw new RuntimeException("Pricing not found with id: " + pricingId);
        }
        
        return convertToResponse(optionalPricing.get());
    }
    
    @Override
    public PricingListResponse getPricingList(Pageable pageable, String search, String pricingType, Boolean isActive) {
        // Use the new findByFilters method without isActive parameter
        Page<Pricing> pricingPage = pricingRepository.findByFilters(pricingType, search, pageable);
        
        List<PricingDetailResponse> packages = pricingPage.getContent().stream()
                .filter(pricing -> isActive == null || pricing.getIsActive() == isActive)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        long totalActive = pricingRepository.findAll().stream().filter(pricing -> pricing.getIsActive() != null && pricing.getIsActive()).count();
        long totalInactive = pricingRepository.findAll().stream().filter(pricing -> pricing.getIsActive() != null && !pricing.getIsActive()).count();
        
        return PricingListResponse.builder()
                .packages(packages)
                .totalElements(pricingPage.getTotalElements())
                .totalPages(pricingPage.getTotalPages())
                .currentPage(pricingPage.getNumber())
                .pageSize(pricingPage.getSize())
                .totalPricing(pricingRepository.count())
                .activePricing(totalActive)
                .inactivePricing(totalInactive)
                .basicPricing(0L) // TODO: Implement when pricing types are defined
                .premiumPricing(0L) // TODO: Implement when pricing types are defined
                .enterprisePricing(0L) // TODO: Implement when pricing types are defined
                .build();
    }
    
    @Override
    public List<PricingDetailResponse> getActivePricing() {
        List<Pricing> activePricing = pricingRepository.findAll().stream()
                .filter(pricing -> pricing.getIsActive() != null && pricing.getIsActive())
                .collect(Collectors.toList());
        return activePricing.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PricingDetailResponse> getPricingByType(String pricingType) {
        List<Pricing> pricingByType = pricingRepository.findByPricingType(pricingType);
        return pricingByType.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PricingDetailResponse> getPricingByPriceRange(Double minPrice, Double maxPrice) {
        List<Pricing> pricingInRange = pricingRepository.findByPriceRange(minPrice, maxPrice);
        return pricingInRange.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public Map<String, Object> getPricingStats() {
        Map<String, Object> stats = new HashMap<>();
        List<Pricing> allPricing = pricingRepository.findAll();
        
        long totalActive = allPricing.stream().filter(pricing -> pricing.getIsActive() != null && pricing.getIsActive()).count();
        long totalInactive = allPricing.stream().filter(pricing -> pricing.getIsActive() != null && !pricing.getIsActive()).count();
        
        stats.put("totalPricing", pricingRepository.count());
        stats.put("activePricing", totalActive);
        stats.put("inactivePricing", totalInactive);
        stats.put("basicPricing", 0L); // TODO: Implement when pricing types are defined
        stats.put("premiumPricing", 0L); // TODO: Implement when pricing types are defined
        stats.put("enterprisePricing", 0L); // TODO: Implement when pricing types are defined
        
        return stats;
    }
    
    @Override
    public PricingDetailResponse togglePricingStatus(Long pricingId) {
        Optional<Pricing> optionalPricing = pricingRepository.findById(pricingId);
        if (optionalPricing.isEmpty()) {
            throw new RuntimeException("Pricing not found with id: " + pricingId);
        }
        
        Pricing pricing = optionalPricing.get();
        pricing.setIsActive(!pricing.getIsActive()); // Toggle the status
        pricing.setUpdatedAt(Instant.now());
        pricing.setUpdatedBy(1L); // TODO: Get from current user context
        
        Pricing updatedPricing = pricingRepository.save(pricing);
        return convertToResponse(updatedPricing);
    }
    
    @Override
    public byte[] exportPricingToExcel(String pricingType, Boolean isActive) {
        // TODO: Implement Excel export
        return new byte[0];
    }
    
    private PricingDetailResponse convertToResponse(Pricing pricing) {
        return PricingDetailResponse.builder()
                .id(pricing.getId())
                .pricingType(pricing.getPricingType())
                .description(pricing.getDescription())
                .price(pricing.getPrice())
                .durationDays(pricing.getDurationDays())
                .discountPercent(pricing.getDiscountPercent())
                .isActive(pricing.getIsActive())
                .createdAt(pricing.getCreatedAt())
                .createdBy(pricing.getCreatedBy())
                .updatedAt(pricing.getUpdatedAt())
                .updatedBy(pricing.getUpdatedBy())
                .build();
    }
} 