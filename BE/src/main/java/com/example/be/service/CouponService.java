package com.example.be.service;

import com.example.be.dto.CouponDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;

public interface CouponService {
    CouponDTO createCoupon(CouponDTO couponDTO);

    CouponDTO getCouponById(Long id);

    void deleteCoupon(Long id); // Added

    CouponDTO validateCoupon(String code, BigDecimal orderValue);

    BigDecimal calculateDiscount(String code, BigDecimal orderValue);

    void useCoupon(String code);

    Page<CouponDTO> getCoupons(Pageable pageable);

    Page<CouponDTO> searchCoupons(String keyword, Boolean isActive, Pageable pageable);
}
