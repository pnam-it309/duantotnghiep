package com.example.be.dto;

import com.example.be.entity.Order;
import com.example.be.entity.OrderItem;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDTO {
    private Order order;
    private List<OrderItem> items;
}
