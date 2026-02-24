package com.example.be.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderStatusHistoryDTO {
    private Long id;
    @jakarta.validation.constraints.NotBlank(message = "Status is required")
    private String status;
    private LocalDateTime changedAt;
    private String note;
}
