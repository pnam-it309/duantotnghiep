package com.example.be.service.impl;

import com.example.be.dto.ProductDTO;
import com.example.be.dto.WishlistDTO;
import com.example.be.entity.Product;
import com.example.be.entity.User;
import com.example.be.entity.Wishlist;
import com.example.be.repository.ProductRepository;
import com.example.be.repository.UserRepository;
import com.example.be.repository.WishlistRepository;
import com.example.be.service.WishlistService;
import com.example.be.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final DtoMapper dtoMapper;

    @Override
    @Transactional
    public WishlistDTO addToWishlist(Long userId, Long productId) {
        // Check if already exists
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new RuntimeException("Product already in wishlist");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .product(product)
                .notifyOnSale(true)
                .build();

        Wishlist saved = wishlistRepository.save(wishlist);

        WishlistDTO dto = new WishlistDTO();
        dto.setId(saved.getId());
        dto.setUserId(userId);
        dto.setProductId(productId);
        dto.setProductName(product.getName());
        dto.setProductPrice(product.getPrice().doubleValue());
        dto.setAddedAt(saved.getAddedAt());
        dto.setNotifyOnSale(saved.getNotifyOnSale());

        return dto;
    }

    @Override
    public List<ProductDTO> getWishlistProducts(Long userId) {
        List<Wishlist> wishlists = wishlistRepository.findByUserId(userId);

        return wishlists.stream()
                .map(w -> dtoMapper.toProductDTO(w.getProduct()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeFromWishlist(Long userId, Long productId) {
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Override
    public boolean isInWishlist(Long userId, Long productId) {
        return wishlistRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    public long getWishlistCount(Long userId) {
        return wishlistRepository.countByUserId(userId);
    }
}
