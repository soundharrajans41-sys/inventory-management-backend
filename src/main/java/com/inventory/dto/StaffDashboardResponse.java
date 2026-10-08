package com.inventory.dto;

public class StaffDashboardResponse {

    private long recentEntriesCount;
    private long ordersProcessedCount;
    private long reportsViewedCount;
    private long pendingTasksCount;

    public StaffDashboardResponse() {}

    public StaffDashboardResponse(long recentEntriesCount, long ordersProcessedCount, long reportsViewedCount, long pendingTasksCount) {
        this.recentEntriesCount = recentEntriesCount;
        this.ordersProcessedCount = ordersProcessedCount;
        this.reportsViewedCount = reportsViewedCount;
        this.pendingTasksCount = pendingTasksCount;
    }

    public long getRecentEntriesCount() { return recentEntriesCount; }
    public void setRecentEntriesCount(long recentEntriesCount) { this.recentEntriesCount = recentEntriesCount; }
    public long getOrdersProcessedCount() { return ordersProcessedCount; }
    public void setOrdersProcessedCount(long ordersProcessedCount) { this.ordersProcessedCount = ordersProcessedCount; }
    public long getReportsViewedCount() { return reportsViewedCount; }
    public void setReportsViewedCount(long reportsViewedCount) { this.reportsViewedCount = reportsViewedCount; }
    public long getPendingTasksCount() { return pendingTasksCount; }
    public void setPendingTasksCount(long pendingTasksCount) { this.pendingTasksCount = pendingTasksCount; }
}
