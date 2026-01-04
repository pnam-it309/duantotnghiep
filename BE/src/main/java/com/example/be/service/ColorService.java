package com.example.be.service;

import com.example.be.entity.Color;
import java.util.List;
import java.util.Optional;

public interface ColorService {
    List<Color> getAllColors();

    Optional<Color> getColorById(Long id);

    Color saveColor(Color color);

    void deleteColor(Long id);
}
