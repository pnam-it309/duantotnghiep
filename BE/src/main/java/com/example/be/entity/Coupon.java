package com.example.be.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    @Column(name = "min_order_value")
    private Integer minOrderValue;

    @Column(name = "discount_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private DiscountType discountType; // PERCENT, FIXED_AMOUNT

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "max_usage")
    private Integer maxUsage;

    @Column(name = "current_usage")
    private Integer currentUsage;

    @Column(name = "is_active")
    private Boolean isActive;

    @PrePersist
    public void prePersist() {
        if (currentUsage == null)
            currentUsage = 0;
        if (isActive == null)
            isActive = true;
        if (discountType == null)
            discountType = DiscountType.FIXED_AMOUNT; // Default
    }

    public enum DiscountType {
        PERCENT, FIXED_AMOUNT
    }
}
