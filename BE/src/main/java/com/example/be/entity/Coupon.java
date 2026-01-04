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

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
}
