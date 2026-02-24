package com.example.be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Long id;
    @jakarta.validation.constraints.NotBlank(message = "Content is required")
    private String content;
    @jakarta.validation.constraints.NotNull(message = "Rating is required")
    @jakarta.validation.constraints.Min(value = 1, message = "Rating must be at least 1")
    @jakarta.validation.constraints.Max(value = 5, message = "Rating cannot exceed 5")
    private Integer rating;
    private LocalDateTime createdAt;
    @jakarta.validation.constraints.NotNull(message = "Product ID is required")
    private Long productId;
    private Long userId;
    private String username; // For display
    private java.util.List<String> imageUrls;
    private Boolean isVerifiedPurchase;
}
