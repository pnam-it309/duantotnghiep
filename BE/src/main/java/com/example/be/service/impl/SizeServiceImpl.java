package com.example.be.service.impl;

import com.example.be.entity.Size;
import com.example.be.repository.SizeRepository;
import com.example.be.service.SizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SizeServiceImpl implements SizeService {
    private final SizeRepository sizeRepository;

    @Override
    public List<Size> getAllSizes() {
        return sizeRepository.findAll();
    }

    @Override
    public Page<Size> getSizes(Pageable pageable) {
        return sizeRepository.findAll(pageable);
    }

    @Override
    public Page<Size> searchSizes(String keyword, Pageable pageable) {
        return sizeRepository.searchSizes(keyword, pageable);
    }

    @Override
    public Optional<Size> getSizeById(Long id) {
        return sizeRepository.findById(id);
    }

    @Override
    public Size saveSize(Size size) {
        return sizeRepository.save(size);
    }

    @Override
    public void deleteSize(Long id) {
        sizeRepository.deleteById(id);
    }
}
