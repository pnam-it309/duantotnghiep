package com.example.be.repository;

import com.example.be.entity.LoyaltyProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoyaltyRepository extends JpaRepository<LoyaltyProgram, Long> {
    Optional<LoyaltyProgram> findByUserId(Long userId);
}
