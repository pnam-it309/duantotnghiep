package com.example.be.repository;

import com.example.be.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<UserAddress, Long> {
    List<UserAddress> findByUserId(Long userId);
}
