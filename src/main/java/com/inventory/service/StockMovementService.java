package com.inventory.service;

import com.inventory.dto.StockMovementResponse;
import com.inventory.entity.MovementType;
import com.inventory.entity.Product;
import com.inventory.entity.StockMovement;
import com.inventory.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;

    public StockMovementService(StockMovementRepository stockMovementRepository) {
        this.stockMovementRepository = stockMovementRepository;
    }

    public List<StockMovementResponse> getAllMovements() {
        return stockMovementRepository.findAllByOrderByTimestampDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<StockMovementResponse> getRecentMovements() {
        return stockMovementRepository.findTop10ByOrderByTimestampDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<StockMovementResponse> getMovementsByProductId(Long productId) {
        return stockMovementRepository.findByProductIdOrderByTimestampDesc(productId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public StockMovement recordMovement(Product product, int quantity, MovementType type, int previousStock,
                                        int resultingStock, String referenceOrder, String performedBy, String remarks) {
        StockMovement movement = new StockMovement(
                product,
                quantity,
                type,
                previousStock,
                resultingStock,
                referenceOrder,
                performedBy,
                remarks
        );
        return stockMovementRepository.save(movement);
    }

    public StockMovementResponse mapToResponse(StockMovement movement) {
        StockMovementResponse response = new StockMovementResponse();
        response.setId(movement.getId());
        if (movement.getProduct() != null) {
            response.setProductId(movement.getProduct().getId());
            response.setProductName(movement.getProduct().getName());
        }
        response.setQuantity(movement.getQuantity());
        response.setMovementType(movement.getMovementType() != null ? movement.getMovementType().name() : null);
        response.setPreviousStock(movement.getPreviousStock());
        response.setResultingStock(movement.getResultingStock());
        response.setReferenceOrder(movement.getReferenceOrder());
        response.setPerformedBy(movement.getPerformedBy());
        response.setRemarks(movement.getRemarks());
        response.setTimestamp(movement.getTimestamp());
        return response;
    }
}
