package com.example.be.service;

import com.example.be.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface UserService {
    Page<User> getAllUsers(Pageable pageable);

    Optional<User> getUserById(Long id);

    Optional<User> getUserByUsername(String username);

    User saveUser(User user);

    void deleteUser(Long id);

    Page<User> searchUsers(String keyword, String role, Pageable pageable);
}
