package com.example.be.controller;

import com.example.be.dto.ProductVariantDTO;
import com.example.be.entity.Color;
import com.example.be.entity.Product;
import com.example.be.entity.ProductVariant;
import com.example.be.entity.Size;
import com.example.be.exception.ResourceNotFoundException;
import com.example.be.service.ColorService;
import com.example.be.service.ProductService;
import com.example.be.service.ProductVariantService;
import com.example.be.service.SizeService;
import com.example.be.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/product-variants")
@RequiredArgsConstructor
public class ProductVariantController {
    private final ProductVariantService productVariantService;
    private final ProductService productService;
    private final SizeService sizeService;
    private final ColorService colorService;
    private final DtoMapper dtoMapper;

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductVariantDTO>> getVariantsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(productVariantService.getVariantsByProductId(productId).stream()
                .map(dtoMapper::toProductVariantDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductVariantDTO> getVariantById(@PathVariable Long id) {
        ProductVariant variant = productVariantService.getVariantById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + id));
        return ResponseEntity.ok(dtoMapper.toProductVariantDTO(variant));
    }

    @PostMapping
    public ResponseEntity<ProductVariantDTO> createVariant(@RequestBody ProductVariantDTO variantDTO) {
        ProductVariant variant = new ProductVariant();
        mapDTOToVariant(variantDTO, variant);

        ProductVariant savedVariant = productVariantService.saveVariant(variant);
        return ResponseEntity.ok(dtoMapper.toProductVariantDTO(savedVariant));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductVariantDTO> updateVariant(@PathVariable Long id,
            @RequestBody ProductVariantDTO variantDTO) {
        ProductVariant variant = productVariantService.getVariantById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + id));

        mapDTOToVariant(variantDTO, variant);
        variant.setId(id);

        ProductVariant savedVariant = productVariantService.saveVariant(variant);
        return ResponseEntity.ok(dtoMapper.toProductVariantDTO(savedVariant));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVariant(@PathVariable Long id) {
        productVariantService.getVariantById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + id));
        productVariantService.deleteVariant(id);
        return ResponseEntity.ok().build();
    }

    private void mapDTOToVariant(ProductVariantDTO dto, ProductVariant variant) {
        variant.setPrice(dto.getPrice());
        variant.setStockQuantity(dto.getStockQuantity());
        variant.setSku(dto.getSku());

        if (dto.getProductId() != null) {
            Product product = productService.getProductById(dto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            variant.setProduct(product);
        }

        if (dto.getSizeId() != null) {
            Size size = sizeService.getSizeById(dto.getSizeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Size not found"));
            variant.setSize(size);
        }

        if (dto.getColorId() != null) {
            Color color = colorService.getColorById(dto.getColorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Color not found"));
            variant.setColor(color);
        }
    }
}
