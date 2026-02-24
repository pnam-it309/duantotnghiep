package com.example.be.repository;

import com.example.be.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    @Query("SELECT b FROM Brand b WHERE :keyword IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Brand> searchBrands(String keyword, Pageable pageable);
}
