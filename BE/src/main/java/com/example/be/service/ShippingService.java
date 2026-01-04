package com.example.be.service;

import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class ShippingService {

    public String pushOrderToCarrier(String carrierId, Long orderId) {
        // Mock integration with GHN / Viettel Post
        // In reality, this would call external APIs
        String prefix = carrierId.equalsIgnoreCase("GHN") ? "GHN" : "VTP";
        return prefix + orderId + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
