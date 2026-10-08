package com.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ReturnOrderRequest {

    @NotBlank(message = "Return type is required (CUSTOMER_RETURN or SUPPLIER_RETURN)")
    private String returnType;

    private String referenceOrderNumber;
    private String reason;

    @NotEmpty(message = "Items list cannot be empty")
    private List<ReturnOrderItemRequest> items;

    public ReturnOrderRequest() {}

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getReferenceOrderNumber() {
        return referenceOrderNumber;
    }

    public void setReferenceOrderNumber(String referenceOrderNumber) {
        this.referenceOrderNumber = referenceOrderNumber;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<ReturnOrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<ReturnOrderItemRequest> items) {
        this.items = items;
    }
}
