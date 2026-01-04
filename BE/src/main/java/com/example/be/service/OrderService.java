package com.example.be.service;

import com.example.be.entity.Order;
import com.example.be.entity.OrderItem;
import java.util.List;
import java.util.Optional;

public interface OrderService {
    List<Order> getAllOrders();

    List<Order> getOrdersByUserId(Long userId);

    Optional<Order> getOrderById(Long id);

    Order createOrder(Order order, List<OrderItem> items);

    Order updateOrder(Order order);

    void deleteOrder(Long id);

    List<OrderItem> getOrderItems(Long orderId);

    List<com.example.be.entity.OrderStatusHistory> getOrderHistory(Long orderId);
}
