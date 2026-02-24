package com.example.be.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class NotificationDTO {
    private String type; // ORDER_STATUS, STOCK_ALERT, FLASH_SALE, etc.
    private String title;
    private String message;
    private Object data; // Additional data
    private LocalDateTime timestamp;
}
