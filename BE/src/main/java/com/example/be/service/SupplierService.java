package com.example.be.service;

import com.example.be.dto.SupplierDTO;
import com.example.be.entity.Supplier;
import com.example.be.repository.SupplierRepository;
import com.example.be.util.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupplierService {
    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private DtoMapper dtoMapper;

    public List<SupplierDTO> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(dtoMapper::toSupplierDTO)
                .collect(Collectors.toList());
    }

    public List<SupplierDTO> getActiveSuppliers() {
        return supplierRepository.findByActiveTrue().stream()
                .map(dtoMapper::toSupplierDTO)
                .collect(Collectors.toList());
    }

    public SupplierDTO createSupplier(SupplierDTO dto) {
        Supplier supplier = dtoMapper.toSupplierEntity(dto);
        supplier.setActive(true);
        return dtoMapper.toSupplierDTO(supplierRepository.save(supplier));
    }

    public SupplierDTO updateSupplier(Long id, SupplierDTO dto) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        supplier.setName(dto.getName());
        supplier.setAddress(dto.getAddress());
        supplier.setPhoneNumber(dto.getPhoneNumber());
        supplier.setEmail(dto.getEmail());
        // Do not update active status here usually, or optionally

        return dtoMapper.toSupplierDTO(supplierRepository.save(supplier));
    }

    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        supplier.setActive(false);
        supplierRepository.save(supplier);
    }
}
