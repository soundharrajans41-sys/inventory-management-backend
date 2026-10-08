package com.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DeadStockItemResponse {
    private Long id;
    private String name;
    private String sku;
    private String category;
    private String supplier;
    private Integer currentStock;
    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;
    private BigDecimal estimatedStockValue;
    private LocalDateTime lastSaleDate;
    private Long daysWithoutSale;
    private Long productAgeDays;
    private String status; // DEAD_STOCK, SLOW_MOVING, ACTIVE, NEW_UNSOLD
    private String statusLabel;
    private String recommendedAction;

    public DeadStockItemResponse() {}

    public DeadStockItemResponse(Long id, String name, String sku, String category, String supplier,
                                Integer currentStock, BigDecimal purchasePrice, BigDecimal sellingPrice,
                                BigDecimal estimatedStockValue, LocalDateTime lastSaleDate, Long daysWithoutSale,
                                Long productAgeDays, String status, String statusLabel, String recommendedAction) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.category = category;
        this.supplier = supplier;
        this.currentStock = currentStock;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.estimatedStockValue = estimatedStockValue;
        this.lastSaleDate = lastSaleDate;
        this.daysWithoutSale = daysWithoutSale;
        this.productAgeDays = productAgeDays;
        this.status = status;
        this.statusLabel = statusLabel;
        this.recommendedAction = recommendedAction;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public BigDecimal getEstimatedStockValue() {
        return estimatedStockValue;
    }

    public void setEstimatedStockValue(BigDecimal estimatedStockValue) {
        this.estimatedStockValue = estimatedStockValue;
    }

    public LocalDateTime getLastSaleDate() {
        return lastSaleDate;
    }

    public void setLastSaleDate(LocalDateTime lastSaleDate) {
        this.lastSaleDate = lastSaleDate;
    }

    public Long getDaysWithoutSale() {
        return daysWithoutSale;
    }

    public void setDaysWithoutSale(Long daysWithoutSale) {
        this.daysWithoutSale = daysWithoutSale;
    }

    public Long getProductAgeDays() {
        return productAgeDays;
    }

    public void setProductAgeDays(Long productAgeDays) {
        this.productAgeDays = productAgeDays;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }
}

