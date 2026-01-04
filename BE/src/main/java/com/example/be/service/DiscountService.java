package com.example.be.service;

import com.example.be.entity.Discount;
import java.util.List;
import java.util.Optional;

public interface DiscountService {
    List<Discount> getAllDiscounts();

    List<Discount> getDiscountsByProductId(Long productId);

    Optional<Discount> getDiscountById(Long id);

    Discount saveDiscount(Discount discount);

    void deleteDiscount(Long id);
}
