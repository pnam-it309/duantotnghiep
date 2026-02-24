package com.example.be.repository;

import com.example.be.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT c FROM Category c WHERE :keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Category> searchCategories(String keyword, Pageable pageable);
}
