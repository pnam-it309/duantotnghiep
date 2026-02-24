package com.example.be.service.impl;

import com.example.be.entity.Brand;
import com.example.be.repository.BrandRepository;
import com.example.be.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {
    private final BrandRepository brandRepository;

    @Override
    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    @Override
    public Page<Brand> getBrands(Pageable pageable) {
        return brandRepository.findAll(pageable);
    }

    @Override
    public Page<Brand> searchBrands(String keyword, Pageable pageable) {
        return brandRepository.searchBrands(keyword, pageable);
    }

    @Override
    public Optional<Brand> getBrandById(Long id) {
        return brandRepository.findById(id);
    }

    @Override
    public Brand saveBrand(Brand brand) {
        return brandRepository.save(brand);
    }

    @Override
    public void deleteBrand(Long id) {
        brandRepository.deleteById(id);
    }
}
