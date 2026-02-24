package com.example.be.controller;

import com.example.be.dto.CouponDTO;
import com.example.be.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    public ResponseEntity<Page<CouponDTO>> getAllCoupons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(couponService.getCoupons(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CouponDTO>> searchCoupons(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(couponService.searchCoupons(keyword, isActive, pageable));
    }

    @PostMapping
    public ResponseEntity<CouponDTO> createCoupon(@Valid @RequestBody CouponDTO couponDTO) {
        return ResponseEntity.ok(couponService.createCoupon(couponDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/validate")
    public ResponseEntity<?> validateCoupon(@RequestParam String code, @RequestParam BigDecimal orderValue) {
        try {
            CouponDTO coupon = couponService.validateCoupon(code, orderValue);
            BigDecimal discount = couponService.calculateDiscount(code, orderValue);

            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "coupon", coupon,
                    "discountAmount", discount));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "message", e.getMessage()));
        }
    }
}
