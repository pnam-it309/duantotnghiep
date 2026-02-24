package com.example.be.controller;

import com.example.be.dto.SizeDTO;
import com.example.be.entity.Size;
import com.example.be.exception.ResourceNotFoundException;
import com.example.be.service.SizeService;
import com.example.be.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sizes")
@RequiredArgsConstructor
public class SizeController {
    private final SizeService sizeService;
    private final DtoMapper dtoMapper;

    @GetMapping
    public ResponseEntity<Page<SizeDTO>> getSizes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String keyword) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (keyword != null && !keyword.isEmpty()) {
            return ResponseEntity.ok(sizeService.searchSizes(keyword, pageable).map(dtoMapper::toSizeDTO));
        }
        return ResponseEntity.ok(sizeService.getSizes(pageable).map(dtoMapper::toSizeDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<List<SizeDTO>> getAllSizes() {
        return ResponseEntity.ok(sizeService.getAllSizes().stream()
                .map(dtoMapper::toSizeDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SizeDTO> getSizeById(@PathVariable Long id) {
        Size size = sizeService.getSizeById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Size not found with id: " + id));
        return ResponseEntity.ok(dtoMapper.toSizeDTO(size));
    }

    @PostMapping
    public ResponseEntity<SizeDTO> createSize(@Valid @RequestBody SizeDTO sizeDTO) {
        Size size = dtoMapper.toSizeEntity(sizeDTO);
        Size savedSize = sizeService.saveSize(size);
        return ResponseEntity.ok(dtoMapper.toSizeDTO(savedSize));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SizeDTO> updateSize(@PathVariable Long id, @Valid @RequestBody SizeDTO sizeDTO) {
        sizeService.getSizeById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Size not found with id: " + id));

        Size size = dtoMapper.toSizeEntity(sizeDTO);
        size.setId(id);
        Size savedSize = sizeService.saveSize(size);
        return ResponseEntity.ok(dtoMapper.toSizeDTO(savedSize));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSize(@PathVariable Long id) {
        sizeService.getSizeById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Size not found with id: " + id));
        sizeService.deleteSize(id);
        return ResponseEntity.ok().build();
    }
}
