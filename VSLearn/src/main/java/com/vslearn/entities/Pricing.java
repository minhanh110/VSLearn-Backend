package com.vslearn.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "pricing")
public class Pricing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int UNSIGNED not null")
    private Long id;

    @Size(max = 255)
    @NotNull
    @Column(name = "pricing_type", nullable = false)
    private String pricingType;



    @Column(name = "price", columnDefinition = "int UNSIGNED not null")
    private Long price;

    @Column(name = "duration_days", columnDefinition = "int UNSIGNED not null")
    private Long durationDays;

    @Column(name = "discount_percent", columnDefinition = "double UNSIGNED not null default 0")
    private Double discountPercent;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by", columnDefinition = "int UNSIGNED not null")
    private Long createdBy;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by", columnDefinition = "int UNSIGNED")
    private Long updatedBy;

    @Column(name = "deleted_at")
    private Instant deletedAt;
    @Column(name = "deleted_by", columnDefinition = "int UNSIGNED")
    private Long deletedBy;

    @Column(name = "is_active", columnDefinition = "boolean not null default true")
    private Boolean isActive = true;
}