package com.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SalesOrderResponse {

    private Long id;
    private String orderNumber;
    private String customer; // alias for customerName
    private String customerName;
    private String customerEmail;
    private String status;
    private BigDecimal totalAmount;
    private String total; // formatted string e.g. "₹120.00"
    private int itemsCount; // count of items
    private String notes;
    private String createdBy;
    private LocalDateTime createdAt;
    private List<SalesOrderItemResponse> items;

    public static class SalesOrderItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;

        public SalesOrderItemResponse() {}

        public SalesOrderItemResponse(Long id, Long productId, String productName, Integer quantity, BigDecimal unitPrice, BigDecimal subtotal) {
            this.id = id;
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.subtotal = subtotal;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    }

    public SalesOrderResponse() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCustomer() {
        return customer != null ? customer : customerName;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
        this.customerName = customer;
    }

    public String getCustomerName() {
        return customerName != null ? customerName : customer;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
        this.customer = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
        if (totalAmount != null) {
            this.total = "\u20B9" + totalAmount.setScale(2, java.math.RoundingMode.HALF_UP).toString();
        }
    }

    public String getTotal() {
        return total != null ? total : (totalAmount != null ? "\u20B9" + totalAmount.toString() : "\u20B90.00");
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public int getItemsCount() {
        return itemsCount;
    }

    public void setItemsCount(int itemsCount) {
        this.itemsCount = itemsCount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<SalesOrderItemResponse> getItems() {
        return items;
    }

    public void setItems(List<SalesOrderItemResponse> items) {
        this.items = items;
        if (items != null) {
            this.itemsCount = items.size();
        }
    }
}
