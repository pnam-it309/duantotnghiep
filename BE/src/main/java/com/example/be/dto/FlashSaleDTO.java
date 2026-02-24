package com.example.be.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FlashSaleDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    private Integer discountPercent;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalQuantity;
    private Integer soldQuantity;
    private Integer remainingQuantity;
    private Boolean isActive;
    private String status; // UPCOMING, ONGOING, ENDED
}
