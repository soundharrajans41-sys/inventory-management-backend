package com.inventory.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ReturnOrderResponse {

    private Long id;
    private String returnNumber;
    private String returnType;
    private String referenceOrderNumber;
    private String reason;
    private String status;
    private String createdBy;
    private LocalDateTime createdAt;
    private List<ReturnOrderItemResponse> items;

    public static class ReturnOrderItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private Integer quantity;
        private String remarks;

        public ReturnOrderItemResponse() {}

        public ReturnOrderItemResponse(Long id, Long productId, String productName, Integer quantity, String remarks) {
            this.id = id;
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.remarks = remarks;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }

    public ReturnOrderResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReturnNumber() { return returnNumber; }
    public void setReturnNumber(String returnNumber) { this.returnNumber = returnNumber; }
    public String getReturnType() { return returnType; }
    public void setReturnType(String returnType) { this.returnType = returnType; }
    public String getReferenceOrderNumber() { return referenceOrderNumber; }
    public void setReferenceOrderNumber(String referenceOrderNumber) { this.referenceOrderNumber = referenceOrderNumber; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<ReturnOrderItemResponse> getItems() { return items; }
    public void setItems(List<ReturnOrderItemResponse> items) { this.items = items; }
}
