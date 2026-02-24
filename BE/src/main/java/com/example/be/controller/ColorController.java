package com.example.be.controller;

import com.example.be.dto.ColorDTO;
import com.example.be.entity.Color;
import com.example.be.exception.ResourceNotFoundException;
import com.example.be.service.ColorService;
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
@RequestMapping("/api/colors")
@RequiredArgsConstructor
public class ColorController {
    private final ColorService colorService;
    private final DtoMapper dtoMapper;

    @GetMapping
    public ResponseEntity<Page<ColorDTO>> getColors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String keyword) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (keyword != null && !keyword.isEmpty()) {
            return ResponseEntity.ok(colorService.searchColors(keyword, pageable).map(dtoMapper::toColorDTO));
        }
        return ResponseEntity.ok(colorService.getColors(pageable).map(dtoMapper::toColorDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ColorDTO>> getAllColors() {
        return ResponseEntity.ok(colorService.getAllColors().stream()
                .map(dtoMapper::toColorDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ColorDTO> getColorById(@PathVariable Long id) {
        Color color = colorService.getColorById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Color not found with id: " + id));
        return ResponseEntity.ok(dtoMapper.toColorDTO(color));
    }

    @PostMapping
    public ResponseEntity<ColorDTO> createColor(@Valid @RequestBody ColorDTO colorDTO) {
        Color color = dtoMapper.toColorEntity(colorDTO);
        Color savedColor = colorService.saveColor(color);
        return ResponseEntity.ok(dtoMapper.toColorDTO(savedColor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ColorDTO> updateColor(@PathVariable Long id, @Valid @RequestBody ColorDTO colorDTO) {
        colorService.getColorById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Color not found with id: " + id));

        Color color = dtoMapper.toColorEntity(colorDTO);
        color.setId(id);
        Color savedColor = colorService.saveColor(color);
        return ResponseEntity.ok(dtoMapper.toColorDTO(savedColor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteColor(@PathVariable Long id) {
        colorService.getColorById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Color not found with id: " + id));
        colorService.deleteColor(id);
        return ResponseEntity.ok().build();
    }
}
