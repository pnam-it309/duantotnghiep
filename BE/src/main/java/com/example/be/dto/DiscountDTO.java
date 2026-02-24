package com.example.be.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DiscountDTO {
    private Long id;
    @jakarta.validation.constraints.NotNull(message = "Product ID is required")
    private Long productId;

    private String productName;

    @jakarta.validation.constraints.NotNull(message = "Discount percent is required")
    @jakarta.validation.constraints.DecimalMin(value = "0.0", message = "Discount percent must be at least 0")
    @jakarta.validation.constraints.DecimalMax(value = "100.0", message = "Discount percent cannot exceed 100")
    private BigDecimal discountPercent;

    @jakarta.validation.constraints.NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @jakarta.validation.constraints.NotNull(message = "End date is required")
    private LocalDateTime endDate;

    private Boolean active;
}
