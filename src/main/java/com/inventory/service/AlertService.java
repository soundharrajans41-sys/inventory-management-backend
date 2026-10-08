package com.inventory.service;

import com.inventory.dto.AlertResponse;
import com.inventory.entity.Alert;
import com.inventory.entity.AlertType;
import com.inventory.entity.Product;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.AlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlertService {

    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    public List<AlertResponse> getAllAlerts() {
        return alertRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AlertResponse> getActiveAlerts() {
        return alertRepository.findByIsReadFalseOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public long getActiveAlertsCount() {
        return alertRepository.countByIsReadFalse();
    }

    @Transactional
    public void checkProductAlerts(Product product) {
        if (product == null) return;

        int qty = product.getQuantity() != null ? product.getQuantity() : 0;
        int minStock = product.getMinimumStockLevel() != null ? product.getMinimumStockLevel() : 10;
        String refId = String.valueOf(product.getId());

        if (qty == 0) {
            if (!alertRepository.existsByTypeAndReferenceIdAndIsReadFalse(AlertType.CRITICAL, refId)) {
                Alert alert = new Alert(
                        AlertType.CRITICAL,
                        "Out of Stock",
                        product.getName() + " is completely out of stock!",
                        "PRODUCT",
                        refId
                );
                alertRepository.save(alert);
            }
        } else if (qty <= minStock) {
            if (!alertRepository.existsByTypeAndReferenceIdAndIsReadFalse(AlertType.WARNING, refId)) {
                Alert alert = new Alert(
                        AlertType.WARNING,
                        "Low Stock Warning",
                        product.getName() + " is running low (" + qty + " remaining, minimum threshold: " + minStock + ")",
                        "PRODUCT",
                        refId
                );
                alertRepository.save(alert);
            }
        }
    }

    @Transactional
    public void createAlert(AlertType type, String title, String message, String referenceType, String referenceId) {
        Alert alert = new Alert(type, title, message, referenceType, referenceId);
        alertRepository.save(alert);
    }

    @Transactional
    public AlertResponse markAsRead(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + id));
        alert.setIsRead(true);
        Alert saved = alertRepository.save(alert);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteAlert(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + id));
        alertRepository.delete(alert);
    }

    @Transactional
    public void clearAllAlerts() {
        alertRepository.deleteAll();
    }

    public AlertResponse mapToResponse(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getType().name(),
                alert.getTitle(),
                alert.getMessage(),
                alert.getReferenceType(),
                alert.getReferenceId(),
                alert.getIsRead(),
                alert.getCreatedAt()
        );
    }
}
