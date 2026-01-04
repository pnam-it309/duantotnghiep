package com.example.be.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "goods_receipt_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoodsReceiptDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "goods_receipt_id")
    private GoodsReceipt goodsReceipt;

    @ManyToOne
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    private Integer quantity;

    @Column(name = "import_price")
    private java.math.BigDecimal importPrice;
}
