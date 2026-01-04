package com.example.be.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    private BigDecimal subtotal;

    @Column(name = "discount_total")
    private BigDecimal discountTotal;

    @Column(name = "final_total")
    private BigDecimal finalTotal;

    private String status;

    @Column(name = "points_used")
    private int pointsUsed;

    @Column(name = "points_discount")
    private BigDecimal pointsDiscount;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @Column(name = "carrier_name")
    private String carrierName;

    @Column(name = "tracking_code")
    private String trackingCode;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }
}
