package com.example.be.repository;

import com.example.be.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySlug(String slug);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByBrandId(Long brandId);

    List<Product> findByActiveTrue();
}
