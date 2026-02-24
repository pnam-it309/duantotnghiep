package com.example.be.service.impl;

import com.example.be.entity.Category;
import com.example.be.repository.CategoryRepository;
import com.example.be.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Page<Category> getCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Override
    public Page<Category> searchCategories(String keyword, Pageable pageable) {
        return categoryRepository.searchCategories(keyword, pageable);
    }

    @Override
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
