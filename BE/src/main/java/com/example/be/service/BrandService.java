package com.example.be.service;

import com.example.be.entity.Brand;
import java.util.List;
import java.util.Optional;

public interface BrandService {
    List<Brand> getAllBrands();

    Optional<Brand> getBrandById(Long id);

    Brand saveBrand(Brand brand);

    void deleteBrand(Long id);
}
