package com.example.be.controller;

import com.example.be.dto.ReturnRequestDTO;
import com.example.be.service.ReturnService;
import com.example.be.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
public class ReturnController {
    private final ReturnService returnService;
    private final DtoMapper dtoMapper;

    @GetMapping
    public ResponseEntity<List<ReturnRequestDTO>> getAllRequests() {
        return ResponseEntity.ok(returnService.getAllRequests().stream()
                .map(dtoMapper::toReturnRequestDTO)
                .collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<ReturnRequestDTO> createRequest(@RequestBody Map<String, Object> payload) {
        Long orderId = Long.valueOf(payload.get("orderId").toString());
        String reason = (String) payload.get("reason");

        return ResponseEntity.ok(dtoMapper.toReturnRequestDTO(
                returnService.createRequest(orderId, reason)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ReturnRequestDTO> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(dtoMapper.toReturnRequestDTO(
                returnService.updateStatus(id, status)));
    }
}
