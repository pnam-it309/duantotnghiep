package com.example.be.repository;

import com.example.be.entity.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SizeRepository extends JpaRepository<Size, Long> {
    @Query("SELECT s FROM Size s WHERE :keyword IS NULL OR LOWER(s.sizeValue) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Size> searchSizes(String keyword, Pageable pageable);
}
