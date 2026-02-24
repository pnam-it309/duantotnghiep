package com.example.be.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemDTO {
    private Long id;
    @jakarta.validation.constraints.NotNull(message = "Product variant ID is required")
    private Long productVariantId;
    private String productName; // Optional, useful for display
    private String sku; // Optional
    @jakarta.validation.constraints.NotNull(message = "Quantity is required")
    @jakarta.validation.constraints.Positive(message = "Quantity must be positive")
    private Integer quantity;
    @jakarta.validation.constraints.NotNull(message = "Price is required")
    @jakarta.validation.constraints.PositiveOrZero(message = "Price must be non-negative")
    private BigDecimal price;
}
