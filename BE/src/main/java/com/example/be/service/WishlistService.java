package com.example.be.service;

import com.example.be.dto.ProductDTO;
import com.example.be.dto.WishlistDTO;

import java.util.List;

public interface WishlistService {
    WishlistDTO addToWishlist(Long userId, Long productId);

    List<ProductDTO> getWishlistProducts(Long userId);

    void removeFromWishlist(Long userId, Long productId);

    boolean isInWishlist(Long userId, Long productId);

    long getWishlistCount(Long userId);
}
