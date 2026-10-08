package com.inventory.controller;

import com.inventory.dto.StockMovementResponse;
import com.inventory.service.StockMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock-movements")
@Tag(name = "Stock Movements", description = "Endpoints for auditing inventory transactions and stock movement history")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    public StockMovementController(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    @GetMapping
    @Operation(summary = "Get all stock movement history")
    public ResponseEntity<List<StockMovementResponse>> getAllMovements() {
        return ResponseEntity.ok(stockMovementService.getAllMovements());
    }

    @GetMapping("/recent")
    @Operation(summary = "Get 10 most recent stock movements")
    public ResponseEntity<List<StockMovementResponse>> getRecentMovements() {
        return ResponseEntity.ok(stockMovementService.getRecentMovements());
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get movements for a specific product")
    public ResponseEntity<List<StockMovementResponse>> getMovementsByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(stockMovementService.getMovementsByProductId(productId));
    }
}
