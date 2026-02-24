package com.example.be.controller;

import com.example.be.dto.OrderDTO;
import com.example.be.dto.OrderItemDTO;
import com.example.be.dto.OrderRequestDTO;
import com.example.be.entity.*;
import com.example.be.exception.ResourceNotFoundException;
import com.example.be.service.CouponService;
import com.example.be.service.OrderService;
import com.example.be.service.ProductVariantService;
import com.example.be.service.UserService;
import com.example.be.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final UserService userService;
    private final CouponService couponService;
    private final ProductVariantService productVariantService;
    private final com.example.be.service.ShippingService shippingService;
    private final DtoMapper dtoMapper;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Page<OrderDTO>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(orderService.getAllOrders(pageable)
                .map(this::enrichOrderDTO));
    }

    @GetMapping("/search")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<OrderDTO>> searchOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(orderService.searchOrders(keyword, status, pageable)
                .map(this::enrichOrderDTO));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId).stream()
                .map(this::enrichOrderDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return ResponseEntity.ok(enrichOrderDTO(order));
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<List<OrderItemDTO>> getOrderItems(@PathVariable Long id) {
        orderService.getOrderById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return ResponseEntity.ok(orderService.getOrderItems(id).stream()
                .map(dtoMapper::toOrderItemDTO)
                .collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody OrderRequestDTO orderRequest) {
        if (orderRequest.getItems() == null || orderRequest.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }

        Order order = new Order();
        Order requestOrder = orderRequest.getOrder();
        
        if (requestOrder == null) {
            throw new IllegalArgumentException("Order details are missing");
        }

        order.setSubtotal(requestOrder.getSubtotal());
        order.setDiscountTotal(requestOrder.getDiscountTotal());
        order.setFinalTotal(requestOrder.getFinalTotal());
        order.setStatus("PENDING");
        order.setShippingAddress(requestOrder.getShippingAddress());
        order.setPaymentMethod(requestOrder.getPaymentMethod());
        order.setPhoneNumber(requestOrder.getPhoneNumber());

        if (requestOrder.getUser() != null && requestOrder.getUser().getId() != null) {
            User user = userService.getUserById(requestOrder.getUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            order.setUser(user);
        }

        List<OrderItem> items = new ArrayList<>();
        for (OrderItem itemReq : orderRequest.getItems()) {
            if (itemReq.getQuantity() <= 0) {
                throw new IllegalArgumentException("Item quantity must be positive");
            }

            OrderItem item = new OrderItem();
            item.setQuantity(itemReq.getQuantity());
            item.setPrice(itemReq.getPrice());

            if (itemReq.getProductVariant() != null && itemReq.getProductVariant().getId() != null) {
                ProductVariant variant = productVariantService.getVariantById(itemReq.getProductVariant().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Variant not found"));
                
                if (variant.getStockQuantity() < itemReq.getQuantity()) {
                    throw new IllegalArgumentException("Not enough stock for variant: " + variant.getSku());
                }
                
                item.setProductVariant(variant);
            } else {
                throw new IllegalArgumentException("Product variant is required for each item");
            }
            items.add(item);
        }

        Order savedOrder = orderService.createOrder(order, items);
        return ResponseEntity.ok(enrichOrderDTO(savedOrder));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable Long id, @Valid @RequestBody OrderDTO orderDTO) {
        Order existingOrder = orderService.getOrderById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        existingOrder.setStatus(orderDTO.getStatus());
        // Add other update logic if needed

        Order savedOrder = orderService.updateOrder(existingOrder);
        return ResponseEntity.ok(enrichOrderDTO(savedOrder));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.getOrderById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        orderService.deleteOrder(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<com.example.be.dto.OrderStatusHistoryDTO>> getOrderHistory(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderHistory(id).stream()
                .map(dtoMapper::toOrderStatusHistoryDTO)
                .collect(Collectors.toList()));
    }

    @PostMapping("/{id}/ship")
    public ResponseEntity<OrderDTO> shipOrder(@PathVariable Long id, @RequestParam String carrierId) {
        Order order = orderService.getOrderById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Mock Shipping
        String trackingCode = shippingService.pushOrderToCarrier(carrierId, id);

        order.setCarrierName(carrierId);
        order.setTrackingCode(trackingCode);
        order.setStatus("SHIPPING");

        Order saved = orderService.updateOrder(order);
        return ResponseEntity.ok(enrichOrderDTO(saved));
    }

    private OrderDTO enrichOrderDTO(Order order) {
        OrderDTO dto = dtoMapper.toOrderDTO(order);
        List<OrderItem> items = orderService.getOrderItems(order.getId());
        dto.setItems(items.stream()
                .map(dtoMapper::toOrderItemDTO)
                .collect(Collectors.toList()));
        return dto;
    }
}
