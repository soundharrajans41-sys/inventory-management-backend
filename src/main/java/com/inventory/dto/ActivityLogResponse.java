package com.inventory.dto;

import java.time.LocalDateTime;

public class ActivityLogResponse {
    private Long id;
    private String userEmail;
    private String userName;
    private String action;
    private String module;
    private String description;
    private LocalDateTime timestamp;

    public ActivityLogResponse() {}

    public ActivityLogResponse(Long id, String userEmail, String userName, String action, String module, String description, LocalDateTime timestamp) {
        this.id = id;
        this.userEmail = userEmail;
        this.userName = userName;
        this.action = action;
        this.module = module;
        this.description = description;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
