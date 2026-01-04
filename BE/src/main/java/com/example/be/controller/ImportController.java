package com.example.be.controller;

import com.example.be.service.ExcelImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {

    private final ExcelImportService excelImportService;

    @PostMapping("/brands")
    public ResponseEntity<String> importBrands(@RequestParam("file") MultipartFile file) {
        try {
            excelImportService.importBrands(file);
            return ResponseEntity.ok("Brands imported successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Import failed: " + e.getMessage());
        }
    }

    @PostMapping("/categories")
    public ResponseEntity<String> importCategories(@RequestParam("file") MultipartFile file) {
        try {
            excelImportService.importCategories(file);
            return ResponseEntity.ok("Categories imported successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Import failed: " + e.getMessage());
        }
    }

    @PostMapping("/colors")
    public ResponseEntity<String> importColors(@RequestParam("file") MultipartFile file) {
        try {
            excelImportService.importColors(file);
            return ResponseEntity.ok("Colors imported successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Import failed: " + e.getMessage());
        }
    }

    @PostMapping("/sizes")
    public ResponseEntity<String> importSizes(@RequestParam("file") MultipartFile file) {
        try {
            excelImportService.importSizes(file);
            return ResponseEntity.ok("Sizes imported successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Import failed: " + e.getMessage());
        }
    }

    @PostMapping("/users")
    public ResponseEntity<String> importUsers(@RequestParam("file") MultipartFile file) {
        try {
            excelImportService.importUsers(file);
            return ResponseEntity.ok("Users imported successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Import failed: " + e.getMessage());
        }
    }

    @PostMapping("/products")
    public ResponseEntity<String> importProducts(@RequestParam("file") MultipartFile file) {
        try {
            excelImportService.importProducts(file);
            return ResponseEntity.ok("Products imported successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Import failed: " + e.getMessage());
        }
    }

    @PostMapping("/product-variants")
    public ResponseEntity<String> importProductVariants(@RequestParam("file") MultipartFile file) {
        try {
            excelImportService.importProductVariants(file);
            return ResponseEntity.ok("Variants imported successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Import failed: " + e.getMessage());
        }
    }

    @PostMapping("/coupons")
    public ResponseEntity<String> importCoupons(@RequestParam("file") MultipartFile file) {
        try {
            excelImportService.importCoupons(file);
            return ResponseEntity.ok("Coupons imported successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Import failed: " + e.getMessage());
        }
    }

    @PostMapping("/discounts")
    public ResponseEntity<String> importDiscounts(@RequestParam("file") MultipartFile file) {
        try {
            excelImportService.importDiscounts(file);
            return ResponseEntity.ok("Discounts imported successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Import failed: " + e.getMessage());
        }
    }
}
