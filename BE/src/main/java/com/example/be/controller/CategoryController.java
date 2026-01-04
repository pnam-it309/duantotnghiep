package com.example.be.controller;

import com.example.be.dto.CategoryDTO;
import com.example.be.entity.Category;
import com.example.be.exception.ResourceNotFoundException;
import com.example.be.service.CategoryService;
import com.example.be.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final DtoMapper dtoMapper;

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories().stream()
                .map(dtoMapper::toCategoryDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return ResponseEntity.ok(dtoMapper.toCategoryDTO(category));
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO) {
        Category category = dtoMapper.toCategoryEntity(categoryDTO);
        Category savedCategory = categoryService.saveCategory(category);
        return ResponseEntity.ok(dtoMapper.toCategoryDTO(savedCategory));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id, @RequestBody CategoryDTO categoryDTO) {
        categoryService.getCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        Category category = dtoMapper.toCategoryEntity(categoryDTO);
        category.setId(id);
        Category savedCategory = categoryService.saveCategory(category);
        return ResponseEntity.ok(dtoMapper.toCategoryDTO(savedCategory));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.getCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }
}
