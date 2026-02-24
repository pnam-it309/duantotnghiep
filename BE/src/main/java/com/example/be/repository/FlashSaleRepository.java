package com.example.be.repository;

import com.example.be.entity.FlashSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlashSaleRepository extends JpaRepository<FlashSale, Long> {

    @Query("SELECT f FROM FlashSale f WHERE f.isActive = true " +
            "AND f.startTime <= :now AND f.endTime >= :now")
    List<FlashSale> findActiveFlashSales(LocalDateTime now);

    @Query("SELECT f FROM FlashSale f WHERE f.isActive = true " +
            "AND f.startTime > :now ORDER BY f.startTime ASC")
    List<FlashSale> findUpcomingFlashSales(LocalDateTime now);

    @Query("SELECT f FROM FlashSale f WHERE f.product.id = :productId " +
            "AND f.isActive = true AND f.endTime >= :now")
    List<FlashSale> findActiveFlashSalesByProduct(Long productId, LocalDateTime now);
}
