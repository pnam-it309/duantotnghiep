package com.example.be.service;

import com.example.be.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<Category> getAllCategories();
    
    Page<Category> getCategories(Pageable pageable);
    
    Page<Category> searchCategories(String keyword, Pageable pageable);

    Optional<Category> getCategoryById(Long id);

    Category saveCategory(Category category);

    void deleteCategory(Long id);
}
