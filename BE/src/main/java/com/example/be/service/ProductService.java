package com.example.be.service;

import com.example.be.entity.Product;
import com.example.be.entity.ProductImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface ProductService {
    Page<Product> getAllProducts(Pageable pageable);

    Optional<Product> getProductById(Long id);

    Optional<Product> getProductBySlug(String slug);

    List<Product> getProductsByCategory(Long categoryId);

    List<Product> getProductsByBrand(Long brandId);

    Product saveProduct(Product product);

    void deleteProduct(Long id);

    List<ProductImage> getProductImages(Long productId);

    ProductImage saveProductImage(ProductImage image);

    void deleteProductImage(Long imageId);

    Page<Product> searchProducts(String keyword, Long categoryId, Long brandId, java.math.BigDecimal minPrice,
            java.math.BigDecimal maxPrice, Boolean active, Pageable pageable);
}
