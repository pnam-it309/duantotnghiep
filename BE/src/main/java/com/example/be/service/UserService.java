package com.example.be.service;

import com.example.be.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getAllUsers();

    Optional<User> getUserById(Long id);

    Optional<User> getUserByUsername(String username);

    User saveUser(User user);

    void deleteUser(Long id);
}
