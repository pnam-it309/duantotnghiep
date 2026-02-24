package com.example.be.dto;

import lombok.Data;

@Data
public class SizeDTO {
    private Long id;
    @jakarta.validation.constraints.NotBlank(message = "Size value is required")
    private String sizeValue;
}
