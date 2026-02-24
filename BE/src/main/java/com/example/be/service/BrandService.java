package com.example.be.service;

import com.example.be.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface BrandService {
    List<Brand> getAllBrands();
    
    Page<Brand> getBrands(Pageable pageable);
    
    Page<Brand> searchBrands(String keyword, Pageable pageable);

    Optional<Brand> getBrandById(Long id);

    Brand saveBrand(Brand brand);

    void deleteBrand(Long id);
}
