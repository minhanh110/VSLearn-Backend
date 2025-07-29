package com.vslearn.repository;

import com.vslearn.entities.Pricing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PricingRepository extends JpaRepository<Pricing, Long> {
    
    // Tìm package theo type
    List<Pricing> findByPricingType(String pricingType);
    
    // Tìm package theo khoảng giá
    @Query("SELECT p FROM Pricing p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    List<Pricing> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);
    
    // Đếm số package theo type
    Long countByPricingType(String pricingType);
    
    // Phân trang với filter
    @Query("SELECT p FROM Pricing p WHERE " +
           "(:pricingType IS NULL OR p.pricingType = :pricingType) AND " +
           "(:search IS NULL OR p.pricingType LIKE %:search% OR p.description LIKE %:search%)")
    Page<Pricing> findByFilters(
        @Param("pricingType") String pricingType,
        @Param("search") String search,
        Pageable pageable
    );
} 