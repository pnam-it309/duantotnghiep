package com.example.be.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "goods_receipts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoodsReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne
    @JoinColumn(name = "user_id") // Personnel who created the receipt
    private User user;

    @Column(name = "import_date")
    private LocalDateTime importDate;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "goodsReceipt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GoodsReceiptDetail> details;
}
