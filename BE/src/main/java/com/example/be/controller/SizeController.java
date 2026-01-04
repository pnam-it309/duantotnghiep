package com.example.be.controller;

import com.example.be.dto.SizeDTO;
import com.example.be.entity.Size;
import com.example.be.exception.ResourceNotFoundException;
import com.example.be.service.SizeService;
import com.example.be.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sizes")
@RequiredArgsConstructor
public class SizeController {
    private final SizeService sizeService;
    private final DtoMapper dtoMapper;

    @GetMapping
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
    public ResponseEntity<SizeDTO> createSize(@RequestBody SizeDTO sizeDTO) {
        Size size = dtoMapper.toSizeEntity(sizeDTO);
        Size savedSize = sizeService.saveSize(size);
        return ResponseEntity.ok(dtoMapper.toSizeDTO(savedSize));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SizeDTO> updateSize(@PathVariable Long id, @RequestBody SizeDTO sizeDTO) {
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
