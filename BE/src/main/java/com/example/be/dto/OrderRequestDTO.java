package com.example.be.dto;

import com.example.be.entity.Order;
import com.example.be.entity.OrderItem;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDTO {
    @jakarta.validation.constraints.NotNull(message = "Order information is required")
    @jakarta.validation.Valid
    private Order order;

    @jakarta.validation.constraints.NotEmpty(message = "Order must have at least one item")
    @jakarta.validation.Valid
    private List<OrderItem> items;
}
