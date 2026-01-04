package com.example.be.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponDTO {
    private Long id;
    private String code;
    private BigDecimal discountAmount;
    private Integer minOrderValue;
    private LocalDateTime expiryDate;
}
