package com.inventory.dto;

import java.math.BigDecimal;

public class FinancialReportResponse {

    private BigDecimal totalRevenue;
    private BigDecimal totalCost;
    private BigDecimal netProfit;
    private String totalRevenueFormatted;
    private String totalCostFormatted;
    private String netProfitFormatted;
    private long totalOrders;
    private long totalItemsSold;

    public FinancialReportResponse() {}

    public FinancialReportResponse(BigDecimal totalRevenue, BigDecimal totalCost, long totalOrders, long totalItemsSold) {
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.totalCost = totalCost != null ? totalCost : BigDecimal.ZERO;
        this.netProfit = this.totalRevenue.subtract(this.totalCost);
        this.totalRevenueFormatted = "\u20B9" + String.format(java.util.Locale.US, "%,.2f", this.totalRevenue);
        this.totalCostFormatted = "\u20B9" + String.format(java.util.Locale.US, "%,.2f", this.totalCost);
        this.netProfitFormatted = (this.netProfit.compareTo(BigDecimal.ZERO) < 0
                ? "-\u20B9" + String.format(java.util.Locale.US, "%,.2f", this.netProfit.abs())
                : "\u20B9" + String.format(java.util.Locale.US, "%,.2f", this.netProfit));
        this.totalOrders = totalOrders;
        this.totalItemsSold = totalItemsSold;
    }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
        if (totalRevenue != null) {
            this.totalRevenueFormatted = "\u20B9" + String.format(java.util.Locale.US, "%,.2f", totalRevenue);
        }
    }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
        if (totalCost != null) {
            this.totalCostFormatted = "\u20B9" + String.format(java.util.Locale.US, "%,.2f", totalCost);
        }
    }

    public BigDecimal getNetProfit() { return netProfit; }
    public void setNetProfit(BigDecimal netProfit) {
        this.netProfit = netProfit;
        if (netProfit != null) {
            this.netProfitFormatted = (netProfit.compareTo(BigDecimal.ZERO) < 0
                    ? "-\u20B9" + String.format(java.util.Locale.US, "%,.2f", netProfit.abs())
                    : "\u20B9" + String.format(java.util.Locale.US, "%,.2f", netProfit));
        }
    }

    public String getTotalRevenueFormatted() { return totalRevenueFormatted; }
    public void setTotalRevenueFormatted(String totalRevenueFormatted) { this.totalRevenueFormatted = totalRevenueFormatted; }
    public String getTotalCostFormatted() { return totalCostFormatted; }
    public void setTotalCostFormatted(String totalCostFormatted) { this.totalCostFormatted = totalCostFormatted; }
    public String getNetProfitFormatted() { return netProfitFormatted; }
    public void setNetProfitFormatted(String netProfitFormatted) { this.netProfitFormatted = netProfitFormatted; }
    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
    public long getTotalItemsSold() { return totalItemsSold; }
    public void setTotalItemsSold(long totalItemsSold) { this.totalItemsSold = totalItemsSold; }
}
