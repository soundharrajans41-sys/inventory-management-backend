package com.inventory.service;

import com.inventory.dto.SupplierRequest;
import com.inventory.dto.SupplierResponse;
import com.inventory.entity.Product;
import com.inventory.entity.Supplier;
import com.inventory.exception.DuplicateResourceException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final ActivityLogService activityLogService;

    public SupplierService(SupplierRepository supplierRepository,
                           ProductRepository productRepository,
                           ActivityLogService activityLogService) {
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.activityLogService = activityLogService;
    }

    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public SupplierResponse getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        return mapToResponse(supplier);
    }

    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        String name = request.getName().trim();
        if (supplierRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Supplier with name '" + name + "' already exists");
        }

        Supplier supplier = new Supplier();
        supplier.setName(name);
        supplier.setCompanyName(request.getCompanyName() != null ? request.getCompanyName().trim() : null);
        supplier.setContactPerson(request.getContactPerson());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setGstNumber(request.getGstNumber() != null ? request.getGstNumber().trim() : null);
        supplier.setAddress(request.getAddress());
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            supplier.setStatus(request.getStatus().trim().toUpperCase());
        }

        Supplier saved = supplierRepository.save(supplier);
        activityLogService.log("SUPPLIER_CREATE", "SUPPLIERS", "Created supplier: " + saved.getName());
        return mapToResponse(saved);
    }

    @Transactional
    public SupplierResponse updateSupplier(Long id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        String name = request.getName().trim();
        if (!supplier.getName().equalsIgnoreCase(name) && supplierRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Supplier with name '" + name + "' already exists");
        }

        supplier.setName(name);
        if (request.getCompanyName() != null) supplier.setCompanyName(request.getCompanyName().trim());
        if (request.getContactPerson() != null) supplier.setContactPerson(request.getContactPerson());
        if (request.getEmail() != null) supplier.setEmail(request.getEmail());
        if (request.getPhone() != null) supplier.setPhone(request.getPhone());
        if (request.getGstNumber() != null) supplier.setGstNumber(request.getGstNumber().trim());
        if (request.getAddress() != null) supplier.setAddress(request.getAddress());
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            supplier.setStatus(request.getStatus().trim().toUpperCase());
        }

        Supplier updated = supplierRepository.save(supplier);
        activityLogService.log("SUPPLIER_UPDATE", "SUPPLIERS", "Updated supplier: " + updated.getName());
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        List<Product> products = productRepository.findBySupplierId(id);
        if (!products.isEmpty()) {
            throw new DuplicateResourceException("Cannot delete supplier '" + supplier.getName() + "' because " + products.size() + " product(s) are associated with it");
        }

        supplierRepository.delete(supplier);
        activityLogService.log("SUPPLIER_DELETE", "SUPPLIERS", "Deleted supplier: " + supplier.getName());
    }

    public SupplierResponse mapToResponse(Supplier supplier) {
        long prodCount = 0;
        try {
            List<Product> products = productRepository.findBySupplierId(supplier.getId());
            prodCount = products != null ? products.size() : 0;
        } catch (Exception ignored) {}

        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getCompanyName() != null ? supplier.getCompanyName() : supplier.getName(),
                supplier.getContactPerson(),
                supplier.getEmail(),
                supplier.getPhone(),
                supplier.getGstNumber(),
                supplier.getAddress(),
                supplier.getStatus(),
                prodCount,
                supplier.getCreatedAt()
        );
    }
}
