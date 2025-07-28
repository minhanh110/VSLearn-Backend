package com.vslearn.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingCreateRequest {
    
    @NotBlank(message = "Pricing type is required")
    @Size(max = 255, message = "Pricing type must not exceed 255 characters")
    private String pricingType;
    
    @NotBlank(message = "Package name is required")
    @Size(max = 255, message = "Package name must not exceed 255 characters")
    private String packageName;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private Long price;
    
    @NotNull(message = "Duration days is required")
    @Min(value = 1, message = "Duration days must be at least 1")
    private Long durationDays;
    
    @Min(value = 0, message = "Max vocab count must be greater than or equal to 0")
    private Integer maxVocabCount;
    
    @Min(value = 0, message = "Max test count must be greater than or equal to 0")
    private Integer maxTestCount;
    
    @Builder.Default
    private Boolean isActive = true;
    
    @Builder.Default
    @DecimalMin(value = "0.0", message = "Discount percent must be greater than or equal to 0")
    @DecimalMax(value = "100.0", message = "Discount percent must be less than or equal to 100")
    private Double discountPercent = 0.0;
} 