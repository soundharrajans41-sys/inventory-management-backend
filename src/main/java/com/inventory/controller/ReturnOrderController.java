package com.inventory.controller;

import com.inventory.dto.ReturnOrderRequest;
import com.inventory.dto.ReturnOrderResponse;
import com.inventory.service.ReturnOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/returns")
@Tag(name = "Returns Management", description = "Endpoints for managing customer and supplier returns")
public class ReturnOrderController {

    private final ReturnOrderService returnOrderService;

    public ReturnOrderController(ReturnOrderService returnOrderService) {
        this.returnOrderService = returnOrderService;
    }

    @GetMapping
    @Operation(summary = "List all return orders")
    public ResponseEntity<List<ReturnOrderResponse>> getAllReturnOrders() {
        return ResponseEntity.ok(returnOrderService.getAllReturnOrders());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get return order by ID")
    public ResponseEntity<ReturnOrderResponse> getReturnOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(returnOrderService.getReturnOrderById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Create return order (customer or supplier return)")
    public ResponseEntity<ReturnOrderResponse> createReturnOrder(@Valid @RequestBody ReturnOrderRequest request) {
        ReturnOrderResponse response = returnOrderService.createReturnOrder(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Approve return order")
    public ResponseEntity<ReturnOrderResponse> approveReturnOrder(@PathVariable Long id) {
        return ResponseEntity.ok(returnOrderService.approveReturnOrder(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Reject return order")
    public ResponseEntity<ReturnOrderResponse> rejectReturnOrder(@PathVariable Long id) {
        return ResponseEntity.ok(returnOrderService.rejectReturnOrder(id));
    }

    @PostMapping("/{id}/process")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Process return order (adjusts stock accordingly)")
    public ResponseEntity<ReturnOrderResponse> processReturnOrder(@PathVariable Long id) {
        return ResponseEntity.ok(returnOrderService.processReturnOrder(id));
    }
}
