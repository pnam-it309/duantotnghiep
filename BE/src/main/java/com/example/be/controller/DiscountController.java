package com.example.be.controller;

import com.example.be.dto.DiscountDTO;
import com.example.be.entity.Discount;
import com.example.be.entity.Product;
import com.example.be.exception.ResourceNotFoundException;
import com.example.be.service.DiscountService;
import com.example.be.service.ProductService;
import com.example.be.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
public class DiscountController {
    private final DiscountService discountService;
    private final ProductService productService;
    private final DtoMapper dtoMapper;

    @GetMapping
    public ResponseEntity<List<DiscountDTO>> getAllDiscounts() {
        return ResponseEntity.ok(discountService.getAllDiscounts().stream()
                .map(dtoMapper::toDiscountDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<DiscountDTO>> getDiscountsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(discountService.getDiscountsByProductId(productId).stream()
                .map(dtoMapper::toDiscountDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiscountDTO> getDiscountById(@PathVariable Long id) {
        Discount discount = discountService.getDiscountById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with id: " + id));
        return ResponseEntity.ok(dtoMapper.toDiscountDTO(discount));
    }

    @PostMapping
    public ResponseEntity<DiscountDTO> createDiscount(@RequestBody DiscountDTO discountDTO) {
        Discount discount = new Discount();
        if (discountDTO.getProductId() != null) {
            Product product = productService.getProductById(discountDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            discount.setProduct(product);
        }
        discount.setDiscountPercent(discountDTO.getDiscountPercent());
        discount.setStartDate(discountDTO.getStartDate());
        discount.setEndDate(discountDTO.getEndDate());
        discount.setActive(discountDTO.getActive());

        Discount savedDiscount = discountService.saveDiscount(discount);
        return ResponseEntity.ok(dtoMapper.toDiscountDTO(savedDiscount));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiscountDTO> updateDiscount(@PathVariable Long id, @RequestBody DiscountDTO discountDTO) {
        Discount discount = discountService.getDiscountById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with id: " + id));

        if (discountDTO.getProductId() != null) {
            Product product = productService.getProductById(discountDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            discount.setProduct(product);
        }
        discount.setDiscountPercent(discountDTO.getDiscountPercent());
        discount.setStartDate(discountDTO.getStartDate());
        discount.setEndDate(discountDTO.getEndDate());
        discount.setActive(discountDTO.getActive());
        discount.setId(id);

        Discount savedDiscount = discountService.saveDiscount(discount);
        return ResponseEntity.ok(dtoMapper.toDiscountDTO(savedDiscount));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiscount(@PathVariable Long id) {
        discountService.getDiscountById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with id: " + id));
        discountService.deleteDiscount(id);
        return ResponseEntity.ok().build();
    }
}
