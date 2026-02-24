package com.example.be.service;

import com.example.be.entity.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface SizeService {
    List<Size> getAllSizes();
    
    Page<Size> getSizes(Pageable pageable);
    
    Page<Size> searchSizes(String keyword, Pageable pageable);

    Optional<Size> getSizeById(Long id);

    Size saveSize(Size size);

    void deleteSize(Long id);
}
