package com.example.be.service;

import com.example.be.entity.Order;
import com.example.be.entity.ReturnRequest;
import com.example.be.repository.OrderRepository;
import com.example.be.repository.ReturnRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReturnService {
    private final ReturnRequestRepository returnRequestRepository;
    private final OrderRepository orderRepository;

    public List<ReturnRequest> getAllRequests() {
        return returnRequestRepository.findAll();
    }

    @Transactional
    public ReturnRequest createRequest(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Simple validation
        if (!"DELIVERED".equals(order.getStatus())) {
            throw new RuntimeException("Only delivered orders can be returned");
        }

        ReturnRequest request = ReturnRequest.builder()
                .order(order)
                .reason(reason)
                .status("PENDING")
                .refundAmount(order.getFinalTotal()) // Full refund default
                .build();

        return returnRequestRepository.save(request);
    }

    @Transactional
    public ReturnRequest updateStatus(Long requestId, String status) {
        ReturnRequest request = returnRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        request.setStatus(status);

        if ("APPROVED".equals(status)) {
            // Logic to refund or update order status
            request.getOrder().setStatus("RETURNED");
            orderRepository.save(request.getOrder());
        }

        return returnRequestRepository.save(request);
    }
}
