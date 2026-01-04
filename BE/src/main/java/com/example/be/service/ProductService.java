package com.example.be.service;

import com.example.be.entity.Product;
import com.example.be.entity.ProductImage;
import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<Product> getAllProducts();

    Optional<Product> getProductById(Long id);

    Optional<Product> getProductBySlug(String slug);

    List<Product> getProductsByCategory(Long categoryId);

    List<Product> getProductsByBrand(Long brandId);

    Product saveProduct(Product product);

    void deleteProduct(Long id);

    List<ProductImage> getProductImages(Long productId);

    ProductImage saveProductImage(ProductImage image);

    void deleteProductImage(Long imageId);
}
