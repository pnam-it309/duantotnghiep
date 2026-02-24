package com.example.be.dto;

import lombok.Data;

@Data
public class CategoryDTO {
    private Long id;
    @jakarta.validation.constraints.NotBlank(message = "Category name is required")
    private String name;
}
