package com.vslearn.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "pricing_id", columnList = "pricing_id"),
        @Index(name = "created_by", columnList = "created_by")
}, uniqueConstraints = {
        @UniqueConstraint(name = "code", columnNames = {"code"})
})
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int UNSIGNED not null")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "pricing_id", nullable = false)
    private Pricing pricing;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private Instant endDate;

    @Size(max = 255)
    @NotNull
    @Column(name = "code", nullable = false)
    private String code;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // Payment fields - chỉ thêm những cái cần thiết
    @Column(name = "amount")
    private Double amount;

    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(name = "description")
    private String description;

    public enum PaymentStatus {
        PENDING,    // Chờ thanh toán
        PAID,       // Đã thanh toán
        FAILED,     // Thanh toán thất bại
        EXPIRED     // Hết hạn
    }
}