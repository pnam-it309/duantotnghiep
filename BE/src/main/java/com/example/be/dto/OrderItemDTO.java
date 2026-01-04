package com.example.be.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemDTO {
    private Long id;
    private Long productVariantId;
    private String productName; // Optional, useful for display
    private String sku; // Optional
    private Integer quantity;
    private BigDecimal price;
}
