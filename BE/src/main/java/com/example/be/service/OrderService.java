package com.example.be.service;

import com.example.be.entity.Order;
import com.example.be.entity.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface OrderService {
    Page<Order> getAllOrders(Pageable pageable);

    List<Order> getOrdersByUserId(Long userId);

    Optional<Order> getOrderById(Long id);

    Order createOrder(Order order, List<OrderItem> items);

    Order updateOrder(Order order);

    void deleteOrder(Long id);

    List<OrderItem> getOrderItems(Long orderId);

    List<com.example.be.entity.OrderStatusHistory> getOrderHistory(Long orderId);

    Page<Order> searchOrders(String keyword, String status, Pageable pageable);
}
