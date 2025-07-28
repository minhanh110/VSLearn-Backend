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
        if (request.getPackageName() != null) {
            pricing.setPackageName(request.getPackageName());
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
        if (request.getMaxVocabCount() != null) {
            pricing.setMaxVocabCount(request.getMaxVocabCount());
        }
        if (request.getMaxTestCount() != null) {
            pricing.setMaxTestCount(request.getMaxTestCount());
        }
        if (request.getIsActive() != null) {
            pricing.setIsActive(request.getIsActive());
        }
        if (request.getDiscountPercent() != null) {
            pricing.setDiscountPercent(request.getDiscountPercent());
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
        pricing.setIsActive(false);
        
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
        // TODO: Implement with proper filtering
        Page<Pricing> pricingPage = pricingRepository.findAll(pageable);
        
        List<PricingDetailResponse> packages = pricingPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return PricingListResponse.builder()
                .packages(packages)
                .totalElements(pricingPage.getTotalElements())
                .totalPages(pricingPage.getTotalPages())
                .currentPage(pricingPage.getNumber())
                .pageSize(pricingPage.getSize())
                .totalPricing(pricingRepository.count())
                .activePricing(0L) // TODO: Implement when isActive field is added
                .inactivePricing(0L) // TODO: Implement when isActive field is added
                .basicPricing(0L) // TODO: Implement when pricing types are defined
                .premiumPricing(0L) // TODO: Implement when pricing types are defined
                .enterprisePricing(0L) // TODO: Implement when pricing types are defined
                .build();
    }
    
    @Override
    public List<PricingDetailResponse> getActivePricing() {
        // TODO: Implement when isActive field is added to entity
        List<Pricing> allPricing = pricingRepository.findAll();
        return allPricing.stream()
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
        // TODO: Implement when price range query is needed
        List<Pricing> allPricing = pricingRepository.findAll();
        return allPricing.stream()
                .filter(p -> p.getPrice() >= minPrice && p.getPrice() <= maxPrice)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public Map<String, Object> getPricingStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPricing", pricingRepository.count());
        stats.put("activePricing", 0L); // TODO: Implement when isActive field is added
        stats.put("inactivePricing", 0L); // TODO: Implement when isActive field is added
        stats.put("basicPricing", 0L); // TODO: Implement when pricing types are defined
        stats.put("premiumPricing", 0L); // TODO: Implement when pricing types are defined
        stats.put("enterprisePricing", 0L); // TODO: Implement when pricing types are defined
        
        return stats;
    }
    
    @Override
    public PricingDetailResponse togglePricingStatus(Long pricingId) {
        // TODO: Implement when isActive field is added to entity
        Optional<Pricing> optionalPricing = pricingRepository.findById(pricingId);
        if (optionalPricing.isEmpty()) {
            throw new RuntimeException("Pricing not found with id: " + pricingId);
        }
        
        Pricing pricing = optionalPricing.get();
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
                .packageName("N/A") // TODO: Add packageName field to entity
                .description(pricing.getDescription())
                .price(pricing.getPrice())
                .durationDays(pricing.getDurationDays())
                .maxVocabCount(0) // TODO: Add maxVocabCount field to entity
                .maxTestCount(0) // TODO: Add maxTestCount field to entity
                .isActive(true) // TODO: Add isActive field to entity
                .discountPercent(0.0) // TODO: Add discountPercent field to entity
                .createdAt(pricing.getCreatedAt())
                .createdBy(pricing.getCreatedBy())
                .updatedAt(pricing.getUpdatedAt())
                .updatedBy(pricing.getUpdatedBy())
                .build();
    }
} 