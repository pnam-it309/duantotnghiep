package com.example.be.controller;

import com.example.be.dto.UserDTO;
import com.example.be.entity.User;
import com.example.be.exception.ResourceNotFoundException;
import com.example.be.service.FirebaseService;
import com.example.be.service.UserService;
import com.example.be.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final FirebaseService firebaseService;
    private final DtoMapper dtoMapper;

    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(userService.getAllUsers(pageable)
                .map(dtoMapper::toUserDTO));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserDTO>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(userService.searchUsers(keyword, role, pageable)
                .map(dtoMapper::toUserDTO));
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
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        // Warning: This is a basic mapping. In real apps, handle password encoding!
        User user = dtoMapper.toUserEntity(userDTO);
        User savedUser = userService.saveUser(user);
        return ResponseEntity.ok(dtoMapper.toUserDTO(savedUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
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

    @PostMapping("/{id}/sms")
    public ResponseEntity<String> sendSMS(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        firebaseService.sendSMS(user.getPhoneNumber(), body.get("message"));
        return ResponseEntity.ok("SMS sent successfully");
    }
}
