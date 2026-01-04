package com.example.be.controller;

import com.example.be.dto.CouponDTO;
import com.example.be.entity.Coupon;
import com.example.be.exception.ResourceNotFoundException;
import com.example.be.service.CouponService;
import com.example.be.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;
    private final DtoMapper dtoMapper;

    @GetMapping
    public ResponseEntity<List<CouponDTO>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons().stream()
                .map(dtoMapper::toCouponDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CouponDTO> getCouponById(@PathVariable Long id) {
        Coupon coupon = couponService.getCouponById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + id));
        return ResponseEntity.ok(dtoMapper.toCouponDTO(coupon));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<CouponDTO> getCouponByCode(@PathVariable String code) {
        Coupon coupon = couponService.getCouponByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with code: " + code));
        return ResponseEntity.ok(dtoMapper.toCouponDTO(coupon));
    }

    @PostMapping
    public ResponseEntity<CouponDTO> createCoupon(@RequestBody CouponDTO couponDTO) {
        Coupon coupon = dtoMapper.toCouponEntity(couponDTO);
        Coupon savedCoupon = couponService.saveCoupon(coupon);
        return ResponseEntity.ok(dtoMapper.toCouponDTO(savedCoupon));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CouponDTO> updateCoupon(@PathVariable Long id, @RequestBody CouponDTO couponDTO) {
        couponService.getCouponById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + id));

        Coupon coupon = dtoMapper.toCouponEntity(couponDTO);
        coupon.setId(id);
        Coupon savedCoupon = couponService.saveCoupon(coupon);
        return ResponseEntity.ok(dtoMapper.toCouponDTO(savedCoupon));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        couponService.getCouponById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + id));
        couponService.deleteCoupon(id);
        return ResponseEntity.ok().build();
    }
}
