package com.example.be.service.impl;

import com.example.be.entity.Product;
import com.example.be.entity.ProductImage;
import com.example.be.repository.ProductImageRepository;
import com.example.be.repository.ProductRepository;
import com.example.be.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Optional<Product> getProductBySlug(String slug) {
        return productRepository.findBySlug(slug);
    }

    @Override
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<Product> getProductsByBrand(Long brandId) {
        return productRepository.findByBrandId(brandId);
    }

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public List<ProductImage> getProductImages(Long productId) {
        return productImageRepository.findByProductId(productId);
    }

    @Override
    public ProductImage saveProductImage(ProductImage image) {
        return productImageRepository.save(image);
    }

    @Override
    public void deleteProductImage(Long imageId) {
        productImageRepository.deleteById(imageId);
    }

    @Override
    public Page<Product> searchProducts(String keyword, Long categoryId, Long brandId, java.math.BigDecimal minPrice,
            java.math.BigDecimal maxPrice, Boolean active, Pageable pageable) {
        return productRepository.searchProducts(keyword, categoryId, brandId, minPrice, maxPrice, active, pageable);
    }
}
