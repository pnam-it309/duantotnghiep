package com.example.be.controller;

import com.example.be.dto.ColorDTO;
import com.example.be.entity.Color;
import com.example.be.exception.ResourceNotFoundException;
import com.example.be.service.ColorService;
import com.example.be.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/colors")
@RequiredArgsConstructor
public class ColorController {
    private final ColorService colorService;
    private final DtoMapper dtoMapper;

    @GetMapping
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
    public ResponseEntity<ColorDTO> createColor(@RequestBody ColorDTO colorDTO) {
        Color color = dtoMapper.toColorEntity(colorDTO);
        Color savedColor = colorService.saveColor(color);
        return ResponseEntity.ok(dtoMapper.toColorDTO(savedColor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ColorDTO> updateColor(@PathVariable Long id, @RequestBody ColorDTO colorDTO) {
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
