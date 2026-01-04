package com.example.be.controller;

import com.example.be.dto.BrandDTO;
import com.example.be.entity.Brand;
import com.example.be.exception.ResourceNotFoundException;
import com.example.be.service.BrandService;
import com.example.be.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {
    private final BrandService brandService;
    private final DtoMapper dtoMapper;

    @GetMapping
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
    public ResponseEntity<BrandDTO> createBrand(@RequestBody BrandDTO brandDTO) {
        Brand brand = dtoMapper.toBrandEntity(brandDTO);
        Brand savedBrand = brandService.saveBrand(brand);
        return ResponseEntity.ok(dtoMapper.toBrandDTO(savedBrand));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BrandDTO> updateBrand(@PathVariable Long id, @RequestBody BrandDTO brandDTO) {
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
