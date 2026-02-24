package com.example.be.controller;

import com.example.be.dto.ReviewDTO;
import com.example.be.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/public/reviews/product/{productId}")
    public ResponseEntity<List<ReviewDTO>> getProductReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId));
    }

    @PostMapping("/reviews")
    public ResponseEntity<ReviewDTO> createReview(@Valid @RequestBody ReviewDTO reviewDTO) {
        return ResponseEntity.ok(reviewService.createReview(reviewDTO));
    }
}
