package com.example.be.service;

import com.example.be.entity.LoyaltyLog;
import com.example.be.entity.Order;
import com.example.be.entity.User;
import com.example.be.repository.LoyaltyLogRepository;
import com.example.be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class LoyaltyService {
    private final UserRepository userRepository;
    private final LoyaltyLogRepository loyaltyLogRepository;

    private static final int POINTS_PER_CURRENCY_UNIT = 1000; // 1000 VND = 1 Point

    @Transactional
    public void processOrderCompletion(Order order) {
        if (order.getUser() == null)
            return;

        User user = order.getUser();

        // Calculate Points: Final Total / 1000
        int pointsEarned = order.getFinalTotal().divide(new BigDecimal(POINTS_PER_CURRENCY_UNIT)).intValue();

        if (pointsEarned > 0) {
            // Update User Points
            user.setRewardPoints(user.getRewardPoints() + pointsEarned);

            // Check Tier Upgrade
            updateMembershipTier(user);

            userRepository.save(user);

            // Create Log
            LoyaltyLog log = LoyaltyLog.builder()
                    .user(user)
                    .order(order)
                    .points(pointsEarned)
                    .reason("Order #" + order.getId() + " Reward")
                    .build();
            loyaltyLogRepository.save(log);
        }
    }

    private void updateMembershipTier(User user) {
        // Simple logic based on current points (accumulated total would be better, but
        // this is VP for now)
        int p = user.getRewardPoints();
        String newTier = "SILVER";

        if (p >= 5000)
            newTier = "DIAMOND";
        else if (p >= 1000)
            newTier = "GOLD";

        user.setMembershipTier(newTier);
        user.setMembershipTier(newTier);
    }

    @Transactional
    public void redeemPoints(User user, int pointsToRedeem, Order order) {
        if (pointsToRedeem <= 0)
            return;

        if (user.getRewardPoints() < pointsToRedeem) {
            throw new RuntimeException("Insufficient points");
        }

        // Deduct Points
        user.setRewardPoints(user.getRewardPoints() - pointsToRedeem);

        // Check Tier Downgrade? Usually we don't downgrade immediately, or tier is
        // based on historical earning.
        // For simplicity, we keep tier as is or recalculate. Let's keep as is.

        userRepository.save(user);

        // Create Log
        LoyaltyLog log = LoyaltyLog.builder()
                .user(user)
                .order(order)
                .points(-pointsToRedeem) // Negative for passing
                .reason("Redeemed for Order #" + order.getId())
                .build();
        loyaltyLogRepository.save(log);
    }
}
