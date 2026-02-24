package com.example.be.repository;

import com.example.be.entity.Discount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {
    List<Discount> findByProductId(Long productId);

    List<Discount> findByActiveTrue();

    @Query("SELECT d FROM Discount d JOIN d.product p WHERE " +
            "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:productId IS NULL OR p.id = :productId) " +
            "AND (:active IS NULL OR d.active = :active)")
    Page<Discount> searchDiscounts(@Param("keyword") String keyword, @Param("productId") Long productId, @Param("active") Boolean active, Pageable pageable);
}
