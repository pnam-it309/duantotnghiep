package com.example.be.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponDTO {
    private Long id;
    @jakarta.validation.constraints.NotBlank(message = "Coupon code is required")
    private String code;

    @jakarta.validation.constraints.NotNull(message = "Discount amount is required")
    @jakarta.validation.constraints.PositiveOrZero(message = "Discount amount must be non-negative")
    private BigDecimal discountAmount;

    @jakarta.validation.constraints.PositiveOrZero(message = "Min order value must be non-negative")
    private Integer minOrderValue;

    @jakarta.validation.constraints.NotNull(message = "Expiry date is required")
    @jakarta.validation.constraints.Future(message = "Expiry date must be in the future")
    private LocalDateTime expiryDate;

    @jakarta.validation.constraints.NotBlank(message = "Discount type is required")
    private String discountType; // "PERCENT" or "FIXED_AMOUNT"

    @jakarta.validation.constraints.Positive(message = "Max usage must be positive")
    private Integer maxUsage;

    private Integer currentUsage;
    private Boolean isActive;
}
