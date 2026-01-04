package com.example.be.service.impl;

import com.example.be.entity.Discount;
import com.example.be.repository.DiscountRepository;
import com.example.be.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {
    private final DiscountRepository discountRepository;

    @Override
    public List<Discount> getAllDiscounts() {
        return discountRepository.findAll();
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
}
