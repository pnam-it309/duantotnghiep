package com.example.be.controller;

import com.example.be.dto.SupplierDTO;
import com.example.be.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {
    @Autowired
    private SupplierService supplierService;

    @GetMapping
    public ResponseEntity<Page<SupplierDTO>> getSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String keyword) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (keyword != null && !keyword.isEmpty()) {
            return ResponseEntity.ok(supplierService.searchSuppliers(keyword, pageable));
        }
        return ResponseEntity.ok(supplierService.getSuppliers(pageable));
    }

    @GetMapping("/all")
    public ResponseEntity<List<SupplierDTO>> getAllSuppliers() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    @GetMapping("/active")
    public ResponseEntity<List<SupplierDTO>> getActiveSuppliers() {
        return ResponseEntity.ok(supplierService.getActiveSuppliers());
    }

    @PostMapping
    public ResponseEntity<SupplierDTO> createSupplier(@Valid @RequestBody SupplierDTO dto) {
        return ResponseEntity.ok(supplierService.createSupplier(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierDTO> updateSupplier(@PathVariable Long id, @Valid @RequestBody SupplierDTO dto) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.ok().build();
    }
}
