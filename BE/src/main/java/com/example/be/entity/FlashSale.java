package com.example.be.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "flash_sales")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashSale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "sale_price", nullable = false)
    private BigDecimal salePrice;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @Column(name = "sold_quantity")
    @Builder.Default
    private Integer soldQuantity = 0;

    @Version
    private Long version; // For optimistic locking

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    public boolean isOngoing() {
        LocalDateTime now = LocalDateTime.now();
        return isActive && now.isAfter(startTime) && now.isBefore(endTime);
    }

    public boolean isUpcoming() {
        return isActive && LocalDateTime.now().isBefore(startTime);
    }

    public boolean hasStock() {
        return soldQuantity < totalQuantity;
    }

    public int getRemainingStock() {
        return totalQuantity - soldQuantity;
    }
}
