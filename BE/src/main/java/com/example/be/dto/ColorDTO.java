package com.example.be.dto;

import lombok.Data;

@Data
public class ColorDTO {
    private Long id;
    @jakarta.validation.constraints.NotBlank(message = "Color name is required")
    private String colorName;
    @jakarta.validation.constraints.NotBlank(message = "Hex code is required")
    @jakarta.validation.constraints.Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Invalid hex code format")
    private String hexCode;
}
