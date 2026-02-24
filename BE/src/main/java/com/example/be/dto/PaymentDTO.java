package com.example.be.dto;

import lombok.Data;

@Data
public class PaymentDTO {
    private long amount;
    private String orderInfo;
}
