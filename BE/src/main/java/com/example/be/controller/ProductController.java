package com.example.be.controller;

import com.example.be.dto.ProductDTO;
import com.example.be.dto.ProductImageDTO;
import com.example.be.entity.Brand;
import com.example.be.entity.Category;
import com.example.be.entity.Product;
import com.example.be.entity.ProductImage;
import com.example.be.exception.ResourceNotFoundException;
import com.example.be.service.BrandService;
import com.example.be.service.CategoryService;
import com.example.be.service.ProductService;
import com.example.be.service.ProductVariantService;
import com.example.be.util.DtoMapper;
import com.example.be.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final ProductVariantService variantService;
    private final CategoryService categoryService;
    private final BrandService brandService;
    private final DtoMapper dtoMapper;
    private final com.example.be.service.RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(productService.getAllProducts(pageable)
                .map(this::enrichProductDTO));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductDTO>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(productService.searchProducts(keyword, categoryId, brandId, minPrice, maxPrice, active, pageable)
                .map(this::enrichProductDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return ResponseEntity.ok(enrichProductDTO(product));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductDTO> getProductBySlug(@PathVariable String slug) {
        Product product = productService.getProductBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
        return ResponseEntity.ok(enrichProductDTO(product));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId).stream()
                .map(this::enrichProductDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/brand/{brandId}")
    public ResponseEntity<List<ProductDTO>> getProductsByBrand(@PathVariable Long brandId) {
        return ResponseEntity.ok(productService.getProductsByBrand(brandId).stream()
                .map(this::enrichProductDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<ProductDTO>> getRecommendations(
            @RequestParam String type,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long productId) {
        return ResponseEntity.ok(recommendationService.getRecommendations(type, userId, productId));
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        Product product = dtoMapper.toProductEntity(productDTO);

        if (product.getSlug() == null || product.getSlug().isEmpty()) {
            product.setSlug(SlugUtil.toSlug(product.getName()));
        }

        if (productDTO.getCategoryId() != null) {
            Category category = categoryService.getCategoryById(productDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            product.setCategory(category);
        }

        if (productDTO.getBrandId() != null) {
            Brand brand = brandService.getBrandById(productDTO.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
            product.setBrand(brand);
        }

        Product savedProduct = productService.saveProduct(product);
        return ResponseEntity.ok(dtoMapper.toProductDTO(savedProduct));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO productDTO) {
        Product existingProduct = productService.getProductById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        Product product = dtoMapper.toProductEntity(productDTO);
        product.setId(id);

        if (product.getSlug() == null || product.getSlug().isEmpty()) {
            product.setSlug(SlugUtil.toSlug(product.getName()));
        }

        if (productDTO.getCategoryId() != null) {
            Category category = categoryService.getCategoryById(productDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            product.setCategory(category);
        } else {
            product.setCategory(existingProduct.getCategory());
        }

        if (productDTO.getBrandId() != null) {
            Brand brand = brandService.getBrandById(productDTO.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
            product.setBrand(brand);
        } else {
            product.setBrand(existingProduct.getBrand());
        }

        Product savedProduct = productService.saveProduct(product);
        return ResponseEntity.ok(dtoMapper.toProductDTO(savedProduct));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.getProductById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    // Helper to add variants and images
    private ProductDTO enrichProductDTO(Product product) {
        ProductDTO dto = dtoMapper.toProductDTO(product);
        dto.setImages(productService.getProductImages(product.getId()).stream()
                .map(dtoMapper::toProductImageDTO)
                .collect(Collectors.toList()));
        dto.setVariants(variantService.getVariantsByProductId(product.getId()).stream()
                .map(dtoMapper::toProductVariantDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    // Product Image endpoints

    @GetMapping("/{id}/images")
    public ResponseEntity<List<ProductImageDTO>> getProductImages(@PathVariable Long id) {
        productService.getProductById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return ResponseEntity.ok(productService.getProductImages(id).stream()
                .map(dtoMapper::toProductImageDTO)
                .collect(Collectors.toList()));
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ProductImageDTO> addProductImage(@PathVariable Long id,
            @Valid @RequestBody ProductImageDTO imageDTO) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        ProductImage image = new ProductImage();
        image.setImageUrl(imageDTO.getImageUrl());
        image.setIsMain(imageDTO.getIsMain());
        image.setProduct(product);

        return ResponseEntity.ok(dtoMapper.toProductImageDTO(productService.saveProductImage(image)));
    }

    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Void> deleteProductImage(@PathVariable Long imageId) {
        productService.deleteProductImage(imageId);
        return ResponseEntity.ok().build();
    }
}
