package com.example.be.service;

import com.example.be.entity.Coupon;
import java.util.List;
import java.util.Optional;

public interface CouponService {
    List<Coupon> getAllCoupons();

    Optional<Coupon> getCouponById(Long id);

    Optional<Coupon> getCouponByCode(String code);

    Coupon saveCoupon(Coupon coupon);

    void deleteCoupon(Long id);
}
