package com.example.be.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoodsReceiptDetailDTO {
    private Long id;
    private Long productVariantId;
    private String productVariantName; // e.g. "iPhone 13 - Red - 128GB"
    private String sku;
    private Integer quantity;
    private java.math.BigDecimal importPrice;
}
