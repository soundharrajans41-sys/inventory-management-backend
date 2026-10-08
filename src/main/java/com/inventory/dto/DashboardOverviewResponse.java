package com.inventory.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardOverviewResponse {

    private BigDecimal totalValue;
    private String totalValueFormatted;
    private long productsCount;
    private long usersCount;
    private long alertsCount;
    private long totalStock;
    private long lowStockCount;
    private long outOfStockCount;
    private long suppliersCount;
    private long pendingPurchases;
    private long pendingSales;
    private long totalSalesOrdersCount;
    private long totalPurchaseOrdersCount;
    private BigDecimal totalSalesValue;
    private BigDecimal totalPurchasesValue;
    private List<StockMovementResponse> recentMovements;
    private List<ProductResponse> lowStockProducts;
    private List<CategoryStockSummary> categoryStock;
    private List<MonthlySalesSummary> monthlySales;
    private List<TopSellingProductSummary> topSellingProducts;
    private List<StockMovementTrendSummary> stockMovementTrends;
    private List<ActivityLogResponse> recentActivities;

    public static class CategoryStockSummary {
        private String category;
        private long productCount;
        private long totalStock;

        public CategoryStockSummary() {}
        public CategoryStockSummary(String category, long productCount, long totalStock) {
            this.category = category;
            this.productCount = productCount;
            this.totalStock = totalStock;
        }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public long getProductCount() { return productCount; }
        public void setProductCount(long productCount) { this.productCount = productCount; }
        public long getTotalStock() { return totalStock; }
        public void setTotalStock(long totalStock) { this.totalStock = totalStock; }
    }

    public static class MonthlySalesSummary {
        private String month;
        private BigDecimal sales;
        private BigDecimal purchases;
        private long orderCount;

        public MonthlySalesSummary() {}
        public MonthlySalesSummary(String month, BigDecimal sales, BigDecimal purchases, long orderCount) {
            this.month = month;
            this.sales = sales;
            this.purchases = purchases;
            this.orderCount = orderCount;
        }
        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public BigDecimal getSales() { return sales; }
        public void setSales(BigDecimal sales) { this.sales = sales; }
        public BigDecimal getPurchases() { return purchases; }
        public void setPurchases(BigDecimal purchases) { this.purchases = purchases; }
        public long getOrderCount() { return orderCount; }
        public void setOrderCount(long orderCount) { this.orderCount = orderCount; }
    }

    public static class TopSellingProductSummary {
        private String name;
        private long quantitySold;
        private BigDecimal totalRevenue;

        public TopSellingProductSummary() {}
        public TopSellingProductSummary(String name, long quantitySold, BigDecimal totalRevenue) {
            this.name = name;
            this.quantitySold = quantitySold;
            this.totalRevenue = totalRevenue;
        }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public long getQuantitySold() { return quantitySold; }
        public void setQuantitySold(long quantitySold) { this.quantitySold = quantitySold; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    }

    public static class StockMovementTrendSummary {
        private String date;
        private long incoming;
        private long outgoing;

        public StockMovementTrendSummary() {}
        public StockMovementTrendSummary(String date, long incoming, long outgoing) {
            this.date = date;
            this.incoming = incoming;
            this.outgoing = outgoing;
        }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public long getIncoming() { return incoming; }
        public void setIncoming(long incoming) { this.incoming = incoming; }
        public long getOutgoing() { return outgoing; }
        public void setOutgoing(long outgoing) { this.outgoing = outgoing; }
    }

    public DashboardOverviewResponse() {}

    public BigDecimal getTotalValue() { return totalValue; }
    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
        if (totalValue != null) {
            this.totalValueFormatted = "\u20B9" + String.format("%,.2f", totalValue);
        }
    }

    public String getTotalValueFormatted() { return totalValueFormatted; }
    public void setTotalValueFormatted(String totalValueFormatted) { this.totalValueFormatted = totalValueFormatted; }
    public long getProductsCount() { return productsCount; }
    public void setProductsCount(long productsCount) { this.productsCount = productsCount; }
    public long getUsersCount() { return usersCount; }
    public void setUsersCount(long usersCount) { this.usersCount = usersCount; }
    public long getAlertsCount() { return alertsCount; }
    public void setAlertsCount(long alertsCount) { this.alertsCount = alertsCount; }
    public long getTotalStock() { return totalStock; }
    public void setTotalStock(long totalStock) { this.totalStock = totalStock; }
    public long getLowStockCount() { return lowStockCount; }
    public void setLowStockCount(long lowStockCount) { this.lowStockCount = lowStockCount; }
    public long getOutOfStockCount() { return outOfStockCount; }
    public void setOutOfStockCount(long outOfStockCount) { this.outOfStockCount = outOfStockCount; }
    public long getSuppliersCount() { return suppliersCount; }
    public void setSuppliersCount(long suppliersCount) { this.suppliersCount = suppliersCount; }
    public long getPendingPurchases() { return pendingPurchases; }
    public void setPendingPurchases(long pendingPurchases) { this.pendingPurchases = pendingPurchases; }
    public long getPendingSales() { return pendingSales; }
    public void setPendingSales(long pendingSales) { this.pendingSales = pendingSales; }
    public long getTotalSalesOrdersCount() { return totalSalesOrdersCount; }
    public void setTotalSalesOrdersCount(long totalSalesOrdersCount) { this.totalSalesOrdersCount = totalSalesOrdersCount; }
    public long getTotalPurchaseOrdersCount() { return totalPurchaseOrdersCount; }
    public void setTotalPurchaseOrdersCount(long totalPurchaseOrdersCount) { this.totalPurchaseOrdersCount = totalPurchaseOrdersCount; }
    public BigDecimal getTotalSalesValue() { return totalSalesValue; }
    public void setTotalSalesValue(BigDecimal totalSalesValue) { this.totalSalesValue = totalSalesValue; }
    public BigDecimal getTotalPurchasesValue() { return totalPurchasesValue; }
    public void setTotalPurchasesValue(BigDecimal totalPurchasesValue) { this.totalPurchasesValue = totalPurchasesValue; }
    public List<StockMovementResponse> getRecentMovements() { return recentMovements; }
    public void setRecentMovements(List<StockMovementResponse> recentMovements) { this.recentMovements = recentMovements; }
    public List<ProductResponse> getLowStockProducts() { return lowStockProducts; }
    public void setLowStockProducts(List<ProductResponse> lowStockProducts) { this.lowStockProducts = lowStockProducts; }
    public List<CategoryStockSummary> getCategoryStock() { return categoryStock; }
    public void setCategoryStock(List<CategoryStockSummary> categoryStock) { this.categoryStock = categoryStock; }
    public List<MonthlySalesSummary> getMonthlySales() { return monthlySales; }
    public void setMonthlySales(List<MonthlySalesSummary> monthlySales) { this.monthlySales = monthlySales; }
    public List<TopSellingProductSummary> getTopSellingProducts() { return topSellingProducts; }
    public void setTopSellingProducts(List<TopSellingProductSummary> topSellingProducts) { this.topSellingProducts = topSellingProducts; }
    public List<StockMovementTrendSummary> getStockMovementTrends() { return stockMovementTrends; }
    public void setStockMovementTrends(List<StockMovementTrendSummary> stockMovementTrends) { this.stockMovementTrends = stockMovementTrends; }
    public List<ActivityLogResponse> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<ActivityLogResponse> recentActivities) { this.recentActivities = recentActivities; }
}
