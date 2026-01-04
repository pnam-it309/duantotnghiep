package com.example.be.service.impl;

import com.example.be.entity.Product;
import com.example.be.entity.ProductImage;
import com.example.be.repository.ProductImageRepository;
import com.example.be.repository.ProductRepository;
import com.example.be.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
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
}
