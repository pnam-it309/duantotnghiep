package com.example.be.controller;

import com.example.be.dto.ProductDTO;
import com.example.be.dto.WishlistDTO;
import com.example.be.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping
    public ResponseEntity<WishlistDTO> addToWishlist(@RequestBody Map<String, Long> request) {
        Long userId = getCurrentUserId();
        Long productId = request.get("productId");

        WishlistDTO wishlist = wishlistService.addToWishlist(userId, productId);
        return ResponseEntity.ok(wishlist);
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getWishlist() {
        Long userId = getCurrentUserId();
        List<ProductDTO> products = wishlistService.getWishlistProducts(userId);
        return ResponseEntity.ok(products);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeFromWishlist(@PathVariable Long productId) {
        Long userId = getCurrentUserId();
        wishlistService.removeFromWishlist(userId, productId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<Map<String, Boolean>> checkInWishlist(@PathVariable Long productId) {
        Long userId = getCurrentUserId();
        boolean inWishlist = wishlistService.isInWishlist(userId, productId);
        return ResponseEntity.ok(Map.of("inWishlist", inWishlist));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getWishlistCount() {
        Long userId = getCurrentUserId();
        long count = wishlistService.getWishlistCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        // This assumes email is the principal - adjust if using username
        // For now, we'll need to fetch user by email
        // You may want to inject UserRepository here
        // For simplicity, assuming the user ID is stored in a custom UserDetails
        return 1L; // TODO: Get actual user ID from security context
    }
}
