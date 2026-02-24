package com.example.be.service;

import com.example.be.dto.ReviewDTO;
import com.example.be.entity.Product;
import com.example.be.entity.Review;
import com.example.be.entity.User;
import com.example.be.exception.ResourceNotFoundException;
import com.example.be.repository.ProductRepository;
import com.example.be.repository.ReviewRepository;
import com.example.be.repository.UserRepository;
import com.example.be.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final DtoMapper dtoMapper;

    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(dtoMapper::toReviewDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewDTO createReview(ReviewDTO reviewDTO) {
        Product product = productRepository.findById(reviewDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        User user = userRepository.findById(reviewDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Review review = dtoMapper.toReviewEntity(reviewDTO);
        review.setProduct(product);
        review.setUser(user);

        Review savedReview = reviewRepository.save(review);
        return dtoMapper.toReviewDTO(savedReview);
    }
}
