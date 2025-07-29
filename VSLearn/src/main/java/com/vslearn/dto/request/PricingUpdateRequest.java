package com.vslearn.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingUpdateRequest {
    
    @Size(max = 255, message = "Pricing type must not exceed 255 characters")
    private String pricingType;
    

    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private Long price;
    
    @Min(value = 1, message = "Duration days must be at least 1")
    private Long durationDays;
    

    
    @DecimalMin(value = "0.0", message = "Discount percent must be greater than or equal to 0")
    @DecimalMax(value = "100.0", message = "Discount percent must be less than or equal to 100")
    private Double discountPercent;
} 