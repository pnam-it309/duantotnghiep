package com.example.be.repository;

import com.example.be.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySlug(String slug);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByBrandId(Long brandId);

    List<Product> findByActiveTrue();

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN p.category c " +
            "LEFT JOIN p.brand b " +
            "LEFT JOIN p.variants v " +
            "WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categoryId IS NULL OR c.id = :categoryId) " +
            "AND (:brandId IS NULL OR b.id = :brandId) " +
            "AND (:minPrice IS NULL OR v.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR v.price <= :maxPrice) " +
            "AND (:active IS NULL OR p.active = :active)")
    Page<Product> searchProducts(@Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            @Param("active") Boolean active,
            Pageable pageable);
}
