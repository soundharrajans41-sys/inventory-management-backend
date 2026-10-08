package com.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    private String description;
    private String status;

    public CategoryRequest() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
