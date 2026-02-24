package com.example.be.repository;

import com.example.be.entity.Color;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ColorRepository extends JpaRepository<Color, Long> {
    @Query("SELECT c FROM Color c WHERE :keyword IS NULL OR LOWER(c.colorName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Color> searchColors(String keyword, Pageable pageable);
}
