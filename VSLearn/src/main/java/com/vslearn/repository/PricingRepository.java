package com.vslearn.repository;

import com.vslearn.entities.Pricing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PricingRepository extends JpaRepository<Pricing, Long> {
    
    // Tìm tất cả packages đang active
    List<Pricing> findByIsActiveTrue();
    
    // Tìm package theo type
    List<Pricing> findByPricingType(String pricingType);
    
    // Tìm package theo type và active
    List<Pricing> findByPricingTypeAndIsActiveTrue(String pricingType);
    
    // Tìm package theo tên (search)
    List<Pricing> findByPackageNameContainingIgnoreCase(String packageName);
    
    // Tìm package theo khoảng giá
    @Query("SELECT p FROM Pricing p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.isActive = true")
    List<Pricing> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);
    
    // Đếm số package theo type
    Long countByPricingType(String pricingType);
    
    // Đếm số package active
    Long countByIsActiveTrue();
    
    // Tìm package theo ID và active
    Optional<Pricing> findByIdAndIsActiveTrue(Long id);
    
    // Phân trang với filter
    @Query("SELECT p FROM Pricing p WHERE " +
           "(:pricingType IS NULL OR p.pricingType = :pricingType) AND " +
           "(:isActive IS NULL OR p.isActive = :isActive) AND " +
           "(:search IS NULL OR p.packageName LIKE %:search% OR p.description LIKE %:search%)")
    Page<Pricing> findByFilters(
        @Param("pricingType") String pricingType,
        @Param("isActive") Boolean isActive,
        @Param("search") String search,
        Pageable pageable
    );
} 