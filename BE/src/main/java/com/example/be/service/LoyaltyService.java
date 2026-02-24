package com.example.be.service;

import com.example.be.entity.LoyaltyProgram;
import com.example.be.entity.User;
import com.example.be.repository.LoyaltyRepository;
import com.example.be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private final LoyaltyRepository loyaltyRepository;
    private final UserRepository userRepository;

    @Transactional
    public void addPoints(Long userId, BigDecimal orderTotal) {
        LoyaltyProgram lp = loyaltyRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId).orElseThrow();
                    return LoyaltyProgram.builder()
                            .user(user)
                            .points(0)
                            .tierLevel("BRONZE")
                            .build();
                });

        // 1000 VND = 1 point
        int earnedPoints = orderTotal.divideToIntegralValue(new BigDecimal("1000")).intValue();
        lp.setPoints(lp.getPoints() + earnedPoints);

        updateTier(lp);

        loyaltyRepository.save(lp);
    }

    public void redeemPoints(User user, Integer points, com.example.be.entity.Order order) {
        LoyaltyProgram lp = loyaltyRepository.findByUserId(user.getId()).orElseThrow();
        if (lp.getPoints() < points) {
            throw new RuntimeException("Insufficient points");
        }
        lp.setPoints(lp.getPoints() - points);
        loyaltyRepository.save(lp);

        // Logic to apply discount to order is handled in OrderService before this
    }

    public void processOrderCompletion(com.example.be.entity.Order order) {
        if (order.getUser() != null) {
            java.math.BigDecimal total = order.getFinalTotal();
            if (total == null)
                total = order.getSubtotal(); // Fallback
            if (total != null) {
                addPoints(order.getUser().getId(), total);
            }
        }
    }

    private void updateTier(LoyaltyProgram lp) {
        if (lp.getPoints() >= 10000)
            lp.setTierLevel("DIAMOND");
        else if (lp.getPoints() >= 5000)
            lp.setTierLevel("GOLD");
        else if (lp.getPoints() >= 1000)
            lp.setTierLevel("SILVER");
        else
            lp.setTierLevel("BRONZE");
    }

    public LoyaltyProgram getLoyaltyInfo(Long userId) {
        return loyaltyRepository.findByUserId(userId)
                .orElse(LoyaltyProgram.builder()
                        .points(0)
                        .tierLevel("BRONZE")
                        .build());
    }
}
