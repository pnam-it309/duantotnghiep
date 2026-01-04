package com.example.be.controller;

import com.example.be.dto.GoodsReceiptDTO;
import com.example.be.service.GoodsReceiptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goods-receipts")
@CrossOrigin(origins = "*")
public class GoodsReceiptController {
    @Autowired
    private GoodsReceiptService goodsReceiptService;

    @GetMapping
    public ResponseEntity<List<GoodsReceiptDTO>> getAllReceipts() {
        return ResponseEntity.ok(goodsReceiptService.getAllReceipts());
    }

    @PostMapping
    public ResponseEntity<GoodsReceiptDTO> createReceipt(@RequestBody GoodsReceiptDTO dto) {
        return ResponseEntity.ok(goodsReceiptService.createReceipt(dto));
    }
}
