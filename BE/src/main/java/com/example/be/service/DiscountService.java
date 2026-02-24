package com.example.be.service;

import com.example.be.entity.Discount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface DiscountService {
    Page<Discount> getAllDiscounts(Pageable pageable);

    List<Discount> getDiscountsByProductId(Long productId);

    Optional<Discount> getDiscountById(Long id);

    Discount saveDiscount(Discount discount);

    void deleteDiscount(Long id);

    Page<Discount> searchDiscounts(String keyword, Long productId, Boolean active, Pageable pageable);
}
