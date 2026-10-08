package com.inventory.dto;

import java.math.BigDecimal;
import java.util.List;

public class DeadStockReportResponse {
    private int totalProductsAnalyzed;
    private int deadStockCount;
    private int slowMovingCount;
    private int activeCount;
    private int newUnsoldCount;
    private BigDecimal totalDeadStockValue;
    private BigDecimal totalSlowMovingValue;
    private BigDecimal totalCapitalTiedUp;
    private int slowThresholdDays;
    private int deadThresholdDays;
    private List<DeadStockItemResponse> items;

    public DeadStockReportResponse() {}

    public DeadStockReportResponse(int totalProductsAnalyzed, int deadStockCount, int slowMovingCount,
                                   int activeCount, int newUnsoldCount, BigDecimal totalDeadStockValue,
                                   BigDecimal totalSlowMovingValue, BigDecimal totalCapitalTiedUp,
                                   int slowThresholdDays, int deadThresholdDays,
                                   List<DeadStockItemResponse> items) {
        this.totalProductsAnalyzed = totalProductsAnalyzed;
        this.deadStockCount = deadStockCount;
        this.slowMovingCount = slowMovingCount;
        this.activeCount = activeCount;
        this.newUnsoldCount = newUnsoldCount;
        this.totalDeadStockValue = totalDeadStockValue;
        this.totalSlowMovingValue = totalSlowMovingValue;
        this.totalCapitalTiedUp = totalCapitalTiedUp;
        this.slowThresholdDays = slowThresholdDays;
        this.deadThresholdDays = deadThresholdDays;
        this.items = items;
    }

    public int getTotalProductsAnalyzed() {
        return totalProductsAnalyzed;
    }

    public void setTotalProductsAnalyzed(int totalProductsAnalyzed) {
        this.totalProductsAnalyzed = totalProductsAnalyzed;
    }

    public int getDeadStockCount() {
        return deadStockCount;
    }

    public void setDeadStockCount(int deadStockCount) {
        this.deadStockCount = deadStockCount;
    }

    public int getSlowMovingCount() {
        return slowMovingCount;
    }

    public void setSlowMovingCount(int slowMovingCount) {
        this.slowMovingCount = slowMovingCount;
    }

    public int getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(int activeCount) {
        this.activeCount = activeCount;
    }

    public int getNewUnsoldCount() {
        return newUnsoldCount;
    }

    public void setNewUnsoldCount(int newUnsoldCount) {
        this.newUnsoldCount = newUnsoldCount;
    }

    public BigDecimal getTotalDeadStockValue() {
        return totalDeadStockValue;
    }

    public void setTotalDeadStockValue(BigDecimal totalDeadStockValue) {
        this.totalDeadStockValue = totalDeadStockValue;
    }

    public BigDecimal getTotalSlowMovingValue() {
        return totalSlowMovingValue;
    }

    public void setTotalSlowMovingValue(BigDecimal totalSlowMovingValue) {
        this.totalSlowMovingValue = totalSlowMovingValue;
    }

    public BigDecimal getTotalCapitalTiedUp() {
        return totalCapitalTiedUp;
    }

    public void setTotalCapitalTiedUp(BigDecimal totalCapitalTiedUp) {
        this.totalCapitalTiedUp = totalCapitalTiedUp;
    }

    public int getSlowThresholdDays() {
        return slowThresholdDays;
    }

    public void setSlowThresholdDays(int slowThresholdDays) {
        this.slowThresholdDays = slowThresholdDays;
    }

    public int getDeadThresholdDays() {
        return deadThresholdDays;
    }

    public void setDeadThresholdDays(int deadThresholdDays) {
        this.deadThresholdDays = deadThresholdDays;
    }

    public List<DeadStockItemResponse> getItems() {
        return items;
    }

    public void setItems(List<DeadStockItemResponse> items) {
        this.items = items;
    }
}

