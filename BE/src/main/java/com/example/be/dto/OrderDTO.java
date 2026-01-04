package com.example.be.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private Long userId;
    private String username;
    private Long couponId;
    private String couponCode;
    private BigDecimal subtotal;
    private BigDecimal discountTotal;
    private BigDecimal finalTotal;
    private String status;
    private String carrierName;
    private String trackingCode;
    private int pointsUsed;
    private BigDecimal pointsDiscount;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> items;
}
