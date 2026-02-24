package com.example.be.controller;

import com.example.be.dto.UserAddressDTO;
import com.example.be.security.JwtTokenProvider;
import com.example.be.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final com.example.be.repository.UserRepository userRepository; // Direct access for ID lookup (quick fix)

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    @GetMapping
    public ResponseEntity<List<UserAddressDTO>> getMyAddresses() {
        return ResponseEntity.ok(addressService.getAddressesByUserId(getCurrentUserId()));
    }

    @PostMapping
    public ResponseEntity<UserAddressDTO> createAddress(@RequestBody UserAddressDTO addressDTO) {
        return ResponseEntity.ok(addressService.createAddress(getCurrentUserId(), addressDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserAddressDTO> updateAddress(@PathVariable Long id, @RequestBody UserAddressDTO addressDTO) {
        // In a real app, verify ownership
        return ResponseEntity.ok(addressService.updateAddress(id, addressDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        // In a real app, verify ownership
        addressService.deleteAddress(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/default")
    public ResponseEntity<UserAddressDTO> setDefaultAddress(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.setDefaultAddress(getCurrentUserId(), id));
    }
}
