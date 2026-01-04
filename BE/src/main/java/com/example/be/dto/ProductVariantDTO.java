package com.example.be.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductVariantDTO {
    private Long id;
    private Long productId;
    private Long sizeId;
    private String sizeValue;
    private Long colorId;
    private String colorName;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Integer stockQuantity;
    private String sku;
}
