package com.example.be.service.impl;

import com.example.be.entity.Discount;
import com.example.be.repository.DiscountRepository;
import com.example.be.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {
    private final DiscountRepository discountRepository;

    @Override
    public Page<Discount> getAllDiscounts(Pageable pageable) {
        return discountRepository.findAll(pageable);
    }

    @Override
    public List<Discount> getDiscountsByProductId(Long productId) {
        return discountRepository.findByProductId(productId);
    }

    @Override
    public Optional<Discount> getDiscountById(Long id) {
        return discountRepository.findById(id);
    }

    @Override
    public Discount saveDiscount(Discount discount) {
        return discountRepository.save(discount);
    }

    @Override
    public void deleteDiscount(Long id) {
        discountRepository.deleteById(id);
    }

    @Override
    public Page<Discount> searchDiscounts(String keyword, Long productId, Boolean active, Pageable pageable) {
        return discountRepository.searchDiscounts(keyword, productId, active, pageable);
    }
}
