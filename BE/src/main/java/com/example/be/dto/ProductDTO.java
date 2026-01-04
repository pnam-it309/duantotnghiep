package com.example.be.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private Boolean active;
    private List<ProductImageDTO> images;
    private List<ProductVariantDTO> variants;
}
