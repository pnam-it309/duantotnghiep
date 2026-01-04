package com.example.be.controller;

import com.example.be.dto.UserDTO;
import com.example.be.entity.User;
import com.example.be.exception.ResourceNotFoundException;
import com.example.be.service.UserService;
import com.example.be.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final DtoMapper dtoMapper;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers().stream()
                .map(dtoMapper::toUserDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return ResponseEntity.ok(dtoMapper.toUserDTO(user));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return ResponseEntity.ok(dtoMapper.toUserDTO(user));
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        // Warning: This is a basic mapping. In real apps, handle password encoding!
        User user = dtoMapper.toUserEntity(userDTO);
        User savedUser = userService.saveUser(user);
        return ResponseEntity.ok(dtoMapper.toUserDTO(savedUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        User user = dtoMapper.toUserEntity(userDTO);
        user.setId(id);
        User savedUser = userService.saveUser(user);
        return ResponseEntity.ok(dtoMapper.toUserDTO(savedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}
