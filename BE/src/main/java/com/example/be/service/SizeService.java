package com.example.be.service;

import com.example.be.entity.Size;
import java.util.List;
import java.util.Optional;

public interface SizeService {
    List<Size> getAllSizes();

    Optional<Size> getSizeById(Long id);

    Size saveSize(Size size);

    void deleteSize(Long id);
}
