package com.example.be.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReturnRequestDTO {
    private Long id;
    private Long orderId;
    private String reason;
    private String status;
    private BigDecimal refundAmount;
    private LocalDateTime createdAt;
}
