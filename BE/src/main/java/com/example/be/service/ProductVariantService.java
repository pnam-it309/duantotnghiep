package com.example.be.service;

import com.example.be.entity.ProductVariant;
import java.util.List;
import java.util.Optional;

public interface ProductVariantService {
    List<ProductVariant> getVariantsByProductId(Long productId);

    Optional<ProductVariant> getVariantById(Long id);

    ProductVariant saveVariant(ProductVariant variant);

    void deleteVariant(Long id);
}
