package com.example.be.dto;

import lombok.Data;

@Data
public class BrandDTO {
    private Long id;
    @jakarta.validation.constraints.NotBlank(message = "Brand name is required")
    private String name;
}
