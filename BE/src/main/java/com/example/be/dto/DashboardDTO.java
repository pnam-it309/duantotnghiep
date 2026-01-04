package com.example.be.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDTO {
    private long totalOrders;
    private long totalProducts;
    private long totalUsers;
    private double totalRevenue;
    private long lowStockCount;

    // For Charts
    private java.util.Map<String, Double> revenueLast7Days;
    private java.util.Map<String, Long> orderStatusCounts;
}
