package com.example.be.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductVariantDTO {
    private Long id;
    private Long productId;
    @jakarta.validation.constraints.NotNull(message = "Size is required")
    private Long sizeId;
    private String sizeValue;
    @jakarta.validation.constraints.NotNull(message = "Color is required")
    private Long colorId;
    private String colorName;
    @jakarta.validation.constraints.NotNull(message = "Price is required")
    @jakarta.validation.constraints.PositiveOrZero(message = "Price must be non-negative")
    private BigDecimal price;
    @jakarta.validation.constraints.PositiveOrZero(message = "Cost price must be non-negative")
    private BigDecimal costPrice;
    @jakarta.validation.constraints.NotNull(message = "Stock quantity is required")
    @jakarta.validation.constraints.PositiveOrZero(message = "Stock quantity must be non-negative")
    private Integer stockQuantity;
    private String sku;
}
