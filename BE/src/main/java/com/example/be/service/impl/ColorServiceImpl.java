package com.example.be.service.impl;

import com.example.be.entity.Color;
import com.example.be.repository.ColorRepository;
import com.example.be.service.ColorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ColorServiceImpl implements ColorService {
    private final ColorRepository colorRepository;

    @Override
    public List<Color> getAllColors() {
        return colorRepository.findAll();
    }

    @Override
    public Page<Color> getColors(Pageable pageable) {
        return colorRepository.findAll(pageable);
    }

    @Override
    public Page<Color> searchColors(String keyword, Pageable pageable) {
        return colorRepository.searchColors(keyword, pageable);
    }

    @Override
    public Optional<Color> getColorById(Long id) {
        return colorRepository.findById(id);
    }

    @Override
    public Color saveColor(Color color) {
        return colorRepository.save(color);
    }

    @Override
    public void deleteColor(Long id) {
        colorRepository.deleteById(id);
    }
}
