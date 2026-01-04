package com.example.be.repository;

import com.example.be.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {
    List<Discount> findByProductId(Long productId);

    List<Discount> findByActiveTrue();
}
