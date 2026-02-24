package com.example.be.dto;

import lombok.Data;

@Data
public class ProductImageDTO {
    private Long id;
    private Long productId;
    @jakarta.validation.constraints.NotBlank(message = "Image URL is required")
    private String imageUrl;
    private Boolean isMain;
}
