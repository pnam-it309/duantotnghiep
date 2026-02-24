package com.example.be.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WishlistDTO {
    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private String productImage;
    private Double productPrice;
    private LocalDateTime addedAt;
    private Boolean notifyOnSale;
}
