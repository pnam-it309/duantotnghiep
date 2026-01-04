package com.example.be.service.impl;

import com.example.be.entity.Coupon;
import com.example.be.repository.CouponRepository;
import com.example.be.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {
    private final CouponRepository couponRepository;

    @Override
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    @Override
    public Optional<Coupon> getCouponById(Long id) {
        return couponRepository.findById(id);
    }

    @Override
    public Optional<Coupon> getCouponByCode(String code) {
        return couponRepository.findByCode(code);
    }

    @Override
    public Coupon saveCoupon(Coupon coupon) {
        return couponRepository.save(coupon);
    }

    @Override
    public void deleteCoupon(Long id) {
        couponRepository.deleteById(id);
    }
}
