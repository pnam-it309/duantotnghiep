package com.example.be.service.impl;

import com.example.be.dto.CouponDTO;
import com.example.be.entity.Coupon;
import com.example.be.repository.CouponRepository;
import com.example.be.service.CouponService;
import com.example.be.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final DtoMapper dtoMapper;

    @Override
    public CouponDTO createCoupon(CouponDTO couponDTO) {
        Coupon coupon = dtoMapper.toCouponEntity(couponDTO);
        if (coupon.getCurrentUsage() == null)
            coupon.setCurrentUsage(0);
        return dtoMapper.toCouponDTO(couponRepository.save(coupon));
    }

    @Override
    public CouponDTO getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
        return dtoMapper.toCouponDTO(coupon);
    }

    @Override
    public void deleteCoupon(Long id) {
        couponRepository.deleteById(id);
    }

    @Override
    public Page<CouponDTO> getCoupons(Pageable pageable) {
        return couponRepository.findAll(pageable).map(dtoMapper::toCouponDTO);
    }

    @Override
    public Page<CouponDTO> searchCoupons(String keyword, Boolean isActive, Pageable pageable) {
        return couponRepository.searchCoupons(keyword, isActive, pageable).map(dtoMapper::toCouponDTO);
    }

    @Override
    public CouponDTO validateCoupon(String code, BigDecimal orderValue) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        if (Boolean.FALSE.equals(coupon.getIsActive())) {
            throw new RuntimeException("Coupon is inactive");
        }

        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Coupon has expired");
        }

        if (coupon.getMaxUsage() != null && coupon.getCurrentUsage() >= coupon.getMaxUsage()) {
            throw new RuntimeException("Coupon usage limit reached");
        }

        if (coupon.getMinOrderValue() != null
                && orderValue.compareTo(BigDecimal.valueOf(coupon.getMinOrderValue())) < 0) {
            throw new RuntimeException("Order value too low for this coupon");
        }

        return dtoMapper.toCouponDTO(coupon);
    }

    @Override
    public BigDecimal calculateDiscount(String code, BigDecimal orderValue) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        // Re-validate just in case (optional, depends on flow)
        // validateCoupon(code, orderValue);

        BigDecimal discount = BigDecimal.ZERO;
        if (coupon.getDiscountType() == Coupon.DiscountType.PERCENT) {
            // Amount is percentage (e.g., 10 for 10%)
            discount = orderValue.multiply(coupon.getDiscountAmount()).divide(BigDecimal.valueOf(100));
        } else {
            // Fixed amount
            discount = coupon.getDiscountAmount();
        }

        // Discount cannot exceed order value
        if (discount.compareTo(orderValue) > 0) {
            discount = orderValue;
        }

        return discount;
    }

    @Override
    @Transactional
    public void useCoupon(String code) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        coupon.setCurrentUsage(coupon.getCurrentUsage() + 1);
        couponRepository.save(coupon);
    }
}
