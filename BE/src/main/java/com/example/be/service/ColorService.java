package com.example.be.service;

import com.example.be.entity.Color;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface ColorService {
    List<Color> getAllColors();
    
    Page<Color> getColors(Pageable pageable);
    
    Page<Color> searchColors(String keyword, Pageable pageable);

    Optional<Color> getColorById(Long id);

    Color saveColor(Color color);

    void deleteColor(Long id);
}
