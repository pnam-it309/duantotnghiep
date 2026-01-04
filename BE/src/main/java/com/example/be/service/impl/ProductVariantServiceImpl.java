package com.example.be.service.impl;

import com.example.be.entity.ProductVariant;
import com.example.be.repository.ProductVariantRepository;
import com.example.be.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {
    private final ProductVariantRepository productVariantRepository;

    @Override
    public List<ProductVariant> getVariantsByProductId(Long productId) {
        return productVariantRepository.findByProductId(productId);
    }

    @Override
    public Optional<ProductVariant> getVariantById(Long id) {
        return productVariantRepository.findById(id);
    }

    @Override
    public ProductVariant saveVariant(ProductVariant variant) {
        return productVariantRepository.save(variant);
    }

    @Override
    public void deleteVariant(Long id) {
        productVariantRepository.deleteById(id);
    }
}
