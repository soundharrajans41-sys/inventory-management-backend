package com.inventory.controller;

import com.inventory.dto.SalesOrderRequest;
import com.inventory.dto.SalesOrderResponse;
import com.inventory.entity.SalesStatus;
import com.inventory.service.SalesOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
@Tag(name = "Sales Orders", description = "Endpoints for customer sales orders, fulfillment, and dispatch")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    public SalesOrderController(SalesOrderService salesOrderService) {
        this.salesOrderService = salesOrderService;
    }

    @GetMapping
    @Operation(summary = "List all sales orders")
    public ResponseEntity<List<SalesOrderResponse>> getAllSalesOrders() {
        return ResponseEntity.ok(salesOrderService.getAllSalesOrders());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sales order by ID")
    public ResponseEntity<SalesOrderResponse> getSalesOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(salesOrderService.getSalesOrderById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Create new sales order")
    public ResponseEntity<SalesOrderResponse> createSalesOrder(@Valid @RequestBody SalesOrderRequest request) {
        SalesOrderResponse response = salesOrderService.createSalesOrder(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/process")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Process and fulfill sales order (reduces inventory atomically)")
    public ResponseEntity<SalesOrderResponse> processSalesOrder(@PathVariable Long id) {
        return ResponseEntity.ok(salesOrderService.processSalesOrder(id));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Update sales order status")
    public ResponseEntity<SalesOrderResponse> updateStatus(@PathVariable Long id, @RequestParam String status) {
        SalesStatus salesStatus = SalesStatus.valueOf(status.trim().toUpperCase());
        return ResponseEntity.ok(salesOrderService.updateOrderStatus(id, salesStatus));
    }
}
