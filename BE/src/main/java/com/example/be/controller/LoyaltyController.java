package com.example.be.controller;

import com.example.be.entity.LoyaltyProgram;
import com.example.be.service.LoyaltyService;
import com.example.be.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    @GetMapping("/my-points")
    public ResponseEntity<LoyaltyProgram> getMyPoints() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // In real app, extract userId from auth
        // Mocking for now as auth might be tricky without token
        // Assuming userId 1 for demo or extract from principals if implemented
        Long userId = 1L;
        try {
            // If we have proper auth details
            if (auth != null
                    && auth.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
                // logic to get ID...
            }
        } catch (Exception e) {
        }

        return ResponseEntity.ok(loyaltyService.getLoyaltyInfo(userId));
    }
}
