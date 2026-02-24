package com.example.be.controller;

import com.example.be.dto.BrandDTO;
import com.example.be.entity.Brand;
import com.example.be.exception.ResourceNotFoundException;
import com.example.be.service.BrandService;
import com.example.be.util.DtoMapper;
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
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {
    private final BrandService brandService;
    private final DtoMapper dtoMapper;

    @GetMapping
    public ResponseEntity<Page<BrandDTO>> getBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String keyword) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        if (keyword != null && !keyword.isEmpty()) {
            return ResponseEntity.ok(brandService.searchBrands(keyword, pageable).map(dtoMapper::toBrandDTO));
        }
        return ResponseEntity.ok(brandService.getBrands(pageable).map(dtoMapper::toBrandDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<List<BrandDTO>> getAllBrands() {
        return ResponseEntity.ok(brandService.getAllBrands().stream()
                .map(dtoMapper::toBrandDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandDTO> getBrandById(@PathVariable Long id) {
        Brand brand = brandService.getBrandById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));
        return ResponseEntity.ok(dtoMapper.toBrandDTO(brand));
    }

    @PostMapping
    public ResponseEntity<BrandDTO> createBrand(@Valid @RequestBody BrandDTO brandDTO) {
        Brand brand = dtoMapper.toBrandEntity(brandDTO);
        Brand savedBrand = brandService.saveBrand(brand);
        return ResponseEntity.ok(dtoMapper.toBrandDTO(savedBrand));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BrandDTO> updateBrand(@PathVariable Long id, @Valid @RequestBody BrandDTO brandDTO) {
        brandService.getBrandById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));

        Brand brand = dtoMapper.toBrandEntity(brandDTO);
        brand.setId(id);
        Brand savedBrand = brandService.saveBrand(brand);
        return ResponseEntity.ok(dtoMapper.toBrandDTO(savedBrand));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
        brandService.getBrandById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));
        brandService.deleteBrand(id);
        return ResponseEntity.ok().build();
    }
}
