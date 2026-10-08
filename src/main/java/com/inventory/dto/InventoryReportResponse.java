package com.inventory.dto;

import java.util.Map;

public class InventoryReportResponse {

    private long totalProducts;
    private long totalStock;
    private long lowStockCount;
    private long outOfStockCount;
    private Map<String, Long> categoryDistribution;

    public InventoryReportResponse() {}

    public InventoryReportResponse(long totalProducts, long totalStock, long lowStockCount, long outOfStockCount, Map<String, Long> categoryDistribution) {
        this.totalProducts = totalProducts;
        this.totalStock = totalStock;
        this.lowStockCount = lowStockCount;
        this.outOfStockCount = outOfStockCount;
        this.categoryDistribution = categoryDistribution;
    }

    public long getTotalProducts() { return totalProducts; }
    public void setTotalProducts(long totalProducts) { this.totalProducts = totalProducts; }
    public long getTotalStock() { return totalStock; }
    public void setTotalStock(long totalStock) { this.totalStock = totalStock; }
    public long getLowStockCount() { return lowStockCount; }
    public void setLowStockCount(long lowStockCount) { this.lowStockCount = lowStockCount; }
    public long getOutOfStockCount() { return outOfStockCount; }
    public void setOutOfStockCount(long outOfStockCount) { this.outOfStockCount = outOfStockCount; }
    public Map<String, Long> getCategoryDistribution() { return categoryDistribution; }
    public void setCategoryDistribution(Map<String, Long> categoryDistribution) { this.categoryDistribution = categoryDistribution; }
}
