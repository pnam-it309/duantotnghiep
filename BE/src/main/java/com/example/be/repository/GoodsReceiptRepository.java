package com.example.be.repository;

import com.example.be.entity.GoodsReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long> {
}
