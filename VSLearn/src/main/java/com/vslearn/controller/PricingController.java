package com.vslearn.controller;

import com.vslearn.dto.request.PricingCreateRequest;
import com.vslearn.dto.request.PricingUpdateRequest;
import com.vslearn.dto.response.PricingDetailResponse;
import com.vslearn.dto.response.PricingListResponse;
import com.vslearn.service.PricingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pricing")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class PricingController {
    
    private final PricingService pricingService;
    
    @Autowired
    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }
    
    // ==================== CRUD OPERATIONS ====================
    
    @PreAuthorize("hasAnyAuthority('ROLE_GENERAL_MANAGER')")
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createPricing(@RequestBody @Valid PricingCreateRequest request) {
        try {
            PricingDetailResponse response = pricingService.createPricing(request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Tạo gói học thành công",
                "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
    
    @PreAuthorize("hasAnyAuthority('ROLE_GENERAL_MANAGER')")
    @GetMapping("/{pricingId}")
    public ResponseEntity<PricingDetailResponse> getPricingById(@PathVariable Long pricingId) {
        PricingDetailResponse response = pricingService.getPricingById(pricingId);
        return ResponseEntity.ok(response);
    }
    
    @PreAuthorize("hasAnyAuthority('ROLE_GENERAL_MANAGER')")
    @PutMapping("/{pricingId}")
    public ResponseEntity<Map<String, Object>> updatePricing(
            @PathVariable Long pricingId,
            @RequestBody @Valid PricingUpdateRequest request) {
        try {
            PricingDetailResponse response = pricingService.updatePricing(pricingId, request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cập nhật gói học thành công",
                "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
    
    @PreAuthorize("hasAnyAuthority('ROLE_GENERAL_MANAGER')")
    @DeleteMapping("/{pricingId}")
    public ResponseEntity<Map<String, Object>> deletePricing(@PathVariable Long pricingId) {
        try {
            pricingService.deletePricing(pricingId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Xóa gói học thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
    
    // ==================== LIST OPERATIONS ====================
    
    @PreAuthorize("hasAnyAuthority('ROLE_GENERAL_MANAGER')")
    @GetMapping("/list")
    public ResponseEntity<PricingListResponse> getPricingList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String pricingType,
            @RequestParam(required = false) Boolean isActive) {
        Pageable pageable = PageRequest.of(page, size);
        PricingListResponse response = pricingService.getPricingList(pageable, search, pricingType, isActive);
        return ResponseEntity.ok(response);
    }
    
    @PreAuthorize("hasAnyAuthority('ROLE_GENERAL_MANAGER')")
    @GetMapping("/active")
    public ResponseEntity<List<PricingDetailResponse>> getActivePricing() {
        List<PricingDetailResponse> response = pricingService.getActivePricing();
        return ResponseEntity.ok(response);
    }
    
    @PreAuthorize("hasAnyAuthority('ROLE_GENERAL_MANAGER')")
    @GetMapping("/type/{pricingType}")
    public ResponseEntity<List<PricingDetailResponse>> getPricingByType(@PathVariable String pricingType) {
        List<PricingDetailResponse> response = pricingService.getPricingByType(pricingType);
        return ResponseEntity.ok(response);
    }
    
    @PreAuthorize("hasAnyAuthority('ROLE_GENERAL_MANAGER')")
    @GetMapping("/price-range")
    public ResponseEntity<List<PricingDetailResponse>> getPricingByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice) {
        List<PricingDetailResponse> response = pricingService.getPricingByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(response);
    }
    
    // ==================== STATISTICS ====================
    
    @PreAuthorize("hasAnyAuthority('ROLE_GENERAL_MANAGER')")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPricingStats() {
        Map<String, Object> stats = pricingService.getPricingStats();
        return ResponseEntity.ok(stats);
    }
    
    // ==================== STATUS MANAGEMENT ====================
    
    @PreAuthorize("hasAnyAuthority('ROLE_GENERAL_MANAGER')")
    @PutMapping("/{pricingId}/toggle-status")
    public ResponseEntity<Map<String, Object>> togglePricingStatus(@PathVariable Long pricingId) {
        try {
            PricingDetailResponse response = pricingService.togglePricingStatus(pricingId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cập nhật trạng thái gói học thành công",
                "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
    
    // ==================== EXPORT FUNCTIONALITY ====================
    
    @PreAuthorize("hasAnyAuthority('ROLE_GENERAL_MANAGER')")
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportPricingToExcel(
            @RequestParam(required = false) String pricingType,
            @RequestParam(required = false) Boolean isActive) {
        try {
            byte[] excelData = pricingService.exportPricingToExcel(pricingType, isActive);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "pricing_export.xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 