package com.example.be.controller;

import com.example.be.dto.PaymentDTO;
import com.example.be.dto.PaymentResponseDTO;
import com.example.be.service.VNPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final VNPayService vnPayService;

    @PostMapping("/create-payment")
    public ResponseEntity<?> createPayment(@RequestBody PaymentDTO paymentDTO) throws UnsupportedEncodingException {
        String paymentUrl = vnPayService.createPaymentUrl(paymentDTO.getAmount(), paymentDTO.getOrderInfo(), null);
        return ResponseEntity.ok(PaymentResponseDTO.builder()
                .status("OK")
                .message("Successfully")
                .url(paymentUrl)
                .build());
    }

    @GetMapping("/vnpay-return")
    public ResponseEntity<?> paymentReturn(
            @RequestParam(value = "vnp_ResponseCode") String responseCode,
            @RequestParam(value = "vnp_TxnRef") String txnRef) {
        // Here you would typically verify the signature and update order status in DB
        // For MVP, we just return the status to frontend which will handle the display
        if ("00".equals(responseCode)) {
            return ResponseEntity.ok(PaymentResponseDTO.builder()
                    .status("OK")
                    .message("Payment Success")
                    .build());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(PaymentResponseDTO.builder()
                    .status("FAILED")
                    .message("Payment Failed")
                    .build());
        }
    }
}
