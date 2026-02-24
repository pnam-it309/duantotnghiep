package com.example.be.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wishlists", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "product_id" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wishlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "added_at")
    private LocalDateTime addedAt;

    @Column(name = "notify_on_sale")
    @Builder.Default
    private Boolean notifyOnSale = true;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }
}
