package com.example.be.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProductDTO {
    private Long id;
    @jakarta.validation.constraints.NotBlank(message = "Name is required")
    private String name;

    private String slug;

    private String description;

    @jakarta.validation.constraints.NotNull(message = "Category is required")
    private Long categoryId;
    private String categoryName;

    @jakarta.validation.constraints.NotNull(message = "Brand is required")
    private Long brandId;
    private String brandName;
    private Boolean active;
    private List<ProductImageDTO> images;
    private List<ProductVariantDTO> variants;
}
