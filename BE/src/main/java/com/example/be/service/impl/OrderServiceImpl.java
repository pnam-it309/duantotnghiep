package com.example.be.service.impl;

import com.example.be.entity.Order;
import com.example.be.entity.OrderItem;
import com.example.be.repository.OrderItemRepository;
import com.example.be.repository.OrderRepository;
import com.example.be.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final com.example.be.repository.OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final com.example.be.repository.ProductVariantRepository productVariantRepository;
    private final com.example.be.service.LoyaltyService loyaltyService;

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    @Transactional
    public Order createOrder(Order order, List<OrderItem> items) {
        // ... (save order) ...
        if (order.getStatus() == null) {
            order.setStatus("PENDING");
        }
        Order savedOrder = orderRepository.save(order);

        // Associate items with the saved order and save them
        if (items != null && !items.isEmpty()) {
            for (OrderItem item : items) {
                // Fetch Variant
                com.example.be.entity.ProductVariant variant = productVariantRepository
                        .findById(item.getProductVariant().getId())
                        .orElseThrow(() -> new RuntimeException("Product Variant not found"));

                // Check Stock
                if (variant.getStockQuantity() < item.getQuantity()) {
                    throw new RuntimeException("Insufficient stock for product: " + variant.getSku());
                }

                // Deduct Stock
                variant.setStockQuantity(variant.getStockQuantity() - item.getQuantity());
                productVariantRepository.save(variant);

                item.setOrder(savedOrder);
                orderItemRepository.save(item);
            }
        }
        // ...

        // Log History
        logStatusChange(savedOrder, "Order Created");

        // Handle Points Redemption
        if (order.getPointsUsed() > 0 && order.getUser() != null) {
            loyaltyService.redeemPoints(order.getUser(), order.getPointsUsed(), savedOrder);
            logStatusChange(savedOrder, "Redeemed " + order.getPointsUsed() + " points");
        }

        return savedOrder;
    }

    @Override
    public Order updateOrder(Order order) {
        Optional<Order> oldOrderOpt = orderRepository.findById(order.getId());
        String oldStatus = oldOrderOpt.map(Order::getStatus).orElse("UNKNOWN");

        Order saved = orderRepository.save(order);

        if (!saved.getStatus().equals(oldStatus)) {
            logStatusChange(saved, "Status updated from " + oldStatus + " to " + saved.getStatus());

            // Loyalty Trigger
            if ("DELIVERED".equals(saved.getStatus())) {
                loyaltyService.processOrderCompletion(saved);
            }
        }
        return saved;
    }

    private void logStatusChange(Order order, String note) {
        com.example.be.entity.OrderStatusHistory history = com.example.be.entity.OrderStatusHistory.builder()
                .order(order)
                .status(order.getStatus())
                .note(note)
                .build();
        orderStatusHistoryRepository.save(history);
    }

    @Override
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    @Override
    public List<com.example.be.entity.OrderStatusHistory> getOrderHistory(Long orderId) {
        return orderStatusHistoryRepository.findByOrderIdOrderByChangedAtDesc(orderId);
    }
}
