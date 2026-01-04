package com.example.be.dto;

import lombok.Data;

@Data
public class ProductImageDTO {
    private Long id;
    private Long productId;
    private String imageUrl;
    private Boolean isMain;
}
