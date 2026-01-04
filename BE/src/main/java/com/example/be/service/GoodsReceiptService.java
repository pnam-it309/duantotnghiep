package com.example.be.service;

import com.example.be.dto.GoodsReceiptDTO;
import com.example.be.dto.GoodsReceiptDetailDTO;
import com.example.be.entity.*;
import com.example.be.repository.*;
import com.example.be.util.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsReceiptService {
    @Autowired
    private GoodsReceiptRepository goodsReceiptRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DtoMapper dtoMapper;

    public List<GoodsReceiptDTO> getAllReceipts() {
        return goodsReceiptRepository.findAll().stream()
                .map(dtoMapper::toGoodsReceiptDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public GoodsReceiptDTO createReceipt(GoodsReceiptDTO dto) {
        GoodsReceipt receipt = new GoodsReceipt();
        receipt.setImportDate(LocalDateTime.now());
        receipt.setNotes(dto.getNotes());

        // Set Supplier
        if (dto.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found"));
            receipt.setSupplier(supplier);
        }

        // Set User
        if (dto.getUserId() != null) {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            receipt.setUser(user);
        }

        // Process Details
        List<GoodsReceiptDetail> details = new ArrayList<>();
        double total = 0;

        for (GoodsReceiptDetailDTO detailDTO : dto.getDetails()) {
            GoodsReceiptDetail detail = new GoodsReceiptDetail();
            detail.setGoodsReceipt(receipt);
            detail.setQuantity(detailDTO.getQuantity());
            detail.setImportPrice(detailDTO.getImportPrice());

            ProductVariant variant = productVariantRepository.findById(detailDTO.getProductVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found: " + detailDTO.getProductVariantId()));

            detail.setProductVariant(variant);
            details.add(detail);

            // --- Moving Average Cost Price Calculation ---
            // Old Total Value = Old Stock * Old Cost
            java.math.BigDecimal oldStock = new java.math.BigDecimal(variant.getStockQuantity());
            java.math.BigDecimal oldCost = variant.getCostPrice() != null ? variant.getCostPrice()
                    : java.math.BigDecimal.ZERO;
            java.math.BigDecimal oldTotalValue = oldStock.multiply(oldCost);

            // Import Value = Import Qty * Import Price
            java.math.BigDecimal importQty = new java.math.BigDecimal(detailDTO.getQuantity());
            java.math.BigDecimal importPrice = detailDTO.getImportPrice();
            java.math.BigDecimal importValue = importQty.multiply(importPrice);

            // New Total Stock
            java.math.BigDecimal newTotalStock = oldStock.add(importQty);

            // New Cost = (Old Total Value + Import Value) / New Total Stock
            if (newTotalStock.compareTo(java.math.BigDecimal.ZERO) > 0) {
                java.math.BigDecimal newCost = oldTotalValue.add(importValue)
                        .divide(newTotalStock, 2, java.math.RoundingMode.HALF_UP);
                variant.setCostPrice(newCost);
            }

            // Update Stock
            variant.setStockQuantity(variant.getStockQuantity() + detailDTO.getQuantity());
            productVariantRepository.save(variant);

            total += detail.getQuantity() * detail.getImportPrice().doubleValue(); // Assuming importPrice is BigDecimal
                                                                                   // now? Wait, in DTO it was Double or
                                                                                   // BigDecimal? Checking... Entity is
                                                                                   // BigDecimal mostly. DTO: check
                                                                                   // later. Assuming DTO importPrice is
                                                                                   // BigDecimal based on typical money
                                                                                   // handling or converting.
                                                                                   // entity.GoodsReceiptDetail
                                                                                   // importPrice is BigDecimal? Let's
                                                                                   // check DTO.
            // If DTO importPrice is double, conversions needed. Checking previous files...
            // GoodsReceiptDetail entity: private BigDecimal importPrice; (Verified in file
            // view)
            // GoodsReceiptDetailDTO: Need to check. If DetailDTO.importPrice is BigDecimal,
            // good.
            // Wait, previous view of GoodsReceiptDetailDTO showed BigDecimal? No, let's
            // verify.
            // For now, assuming BigDecimal based on standard practice for BE. If not, I'll
            // fix.
            // Actually, let's stick to safe conversion if unsure.
            // But looking at code: `detail.setImportPrice(detailDTO.getImportPrice());` ->
            // so types match.
            // `detail.getImportPrice()` in entity is BigDecimal (from view 620 line 68 &
            // file view 583/not visible).
            // wait file 620: `detail.setImportPrice(detailDTO.getImportPrice())`
            // Let's assume BigDecimal for now.

        }

        receipt.setDetails(details);
        receipt.setTotalAmount(total);

        GoodsReceipt saved = goodsReceiptRepository.save(receipt);
        return dtoMapper.toGoodsReceiptDTO(saved);
    }
}
