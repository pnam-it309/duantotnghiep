package com.example.be.repository;

import com.example.be.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);

    @Query("SELECT o FROM Order o WHERE " +
            "(:keyword IS NULL OR CAST(o.id AS string) LIKE CONCAT('%', :keyword, '%') OR " +
            "LOWER(o.user.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(o.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR o.status = :status)")
    Page<Order> searchOrders(@Param("keyword") String keyword, @Param("status") String status, Pageable pageable);
}
