package com.example.be.service;

import com.example.be.dto.ProductDTO;
import com.example.be.entity.Product;
import com.example.be.repository.ProductRepository;
import com.example.be.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final ProductRepository productRepository;
    private final DtoMapper dtoMapper;

    public List<ProductDTO> getRecommendations(String type, Long userId, Long productId) {
        List<Product> products;
        switch (type.toLowerCase()) {
            case "trending":
                // Mock: Get top 10 products (in real app: join with order_items, group by
                // product, order by count desc)
                products = productRepository.findAll().stream().limit(10).collect(Collectors.toList());
                break;
            case "similar":
                if (productId != null) {
                    Product p = productRepository.findById(productId).orElse(null);
                    if (p != null && p.getCategory() != null) {
                        // Find products in same category
                        // This is a naive implementation. Should be repository method
                        // findByCategoryId...
                        products = productRepository.findAll().stream()
                                .filter(prod -> prod.getCategory() != null
                                        && prod.getCategory().getId().equals(p.getCategory().getId())
                                        && !prod.getId().equals(productId))
                                .limit(5)
                                .collect(Collectors.toList());
                    } else {
                        products = List.of();
                    }
                } else {
                    products = List.of();
                }
                break;
            case "foryou":
                // Mock: Random or based on user history (if we had history tracking)
                products = productRepository.findAll().stream().skip(5).limit(5).collect(Collectors.toList());
                break;
            default:
                products = List.of();
        }

        return products.stream().map(dtoMapper::toProductDTO).collect(Collectors.toList());
    }
}
