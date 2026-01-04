package com.example.be.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoodsReceiptDTO {
    private Long id;
    private Long supplierId;
    private String supplierName;
    private Long userId;
    private String username;
    private LocalDateTime importDate;
    private Double totalAmount;
    private String notes;
    private List<GoodsReceiptDetailDTO> details;
}
