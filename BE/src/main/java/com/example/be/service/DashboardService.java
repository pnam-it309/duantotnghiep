package com.example.be.service;

import com.example.be.dto.DashboardDTO;
import com.example.be.repository.*;
import com.example.be.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DashboardService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductVariantRepository productVariantRepository;

    public DashboardDTO getStats() {
        long totalOrders = orderRepository.count();
        long totalProducts = productRepository.count();
        long totalUsers = userRepository.count();
        long lowStockCount = productVariantRepository.countByStockQuantityLessThan(10); // Threshold 10

        List<Order> orders = orderRepository.findAll();
        double totalRevenue = orders.stream()
                .map(Order::getFinalTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();

        // --- Chart Data ---
        // 1. Revenue Last 7 Days
        java.util.Map<String, Double> revenueMap = new java.util.TreeMap<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        // Initialize last 7 days with 0
        for (int i = 6; i >= 0; i--) {
            revenueMap.put(today.minusDays(i).toString(), 0.0);
        }

        for (Order order : orders) {
            if (order.getCreatedAt() != null) {
                String dateStr = order.getCreatedAt().toLocalDate().toString();
                if (revenueMap.containsKey(dateStr)) {
                    double current = revenueMap.get(dateStr);
                    revenueMap.put(dateStr, current + order.getFinalTotal().doubleValue());
                }
            }
        }

        // 2. Order Status Counts
        java.util.Map<String, Long> statusMap = orders.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        o -> o.getStatus() == null ? "UNKNOWN" : o.getStatus(),
                        java.util.stream.Collectors.counting()));

        return DashboardDTO.builder()
                .totalOrders(totalOrders)
                .totalProducts(totalProducts)
                .totalUsers(totalUsers)
                .totalRevenue(totalRevenue)
                .revenueLast7Days(revenueMap)
                .orderStatusCounts(statusMap)
                .build();
    }
}
