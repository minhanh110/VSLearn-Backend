package com.vslearn.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "ticket")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int UNSIGNED not null")
    private Long id;

    @Column(name = "package_type", nullable = false)
    private String packageType;

    @Column(name = "package_price", columnDefinition = "int UNSIGNED not null")
    private Long packagePrice;

    @Column(name = "duration_days", columnDefinition = "int UNSIGNED not null")
    private Long durationDays;

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

/*
 TODO [Reverse Engineering] create field to map the 'discount_percent' column
 Available actions: Define target Java type | Uncomment as is | Remove column mapping
    @Column(name = "discount_percent", columnDefinition = "double UNSIGNED not null")
    private Object discountPercent;
*/
}