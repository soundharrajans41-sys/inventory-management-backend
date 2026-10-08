package com.inventory.dto;

import java.time.LocalDateTime;

public class AlertResponse {

    private Long id;
    private String type;
    private String title;
    private String message;
    private String msg; // alias for message
    private String time; // human readable or formatted
    private String referenceType;
    private String referenceId;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public AlertResponse() {}

    public AlertResponse(Long id, String type, String title, String message, String referenceType, String referenceId, Boolean isRead, LocalDateTime createdAt) {
        this.id = id;
        this.type = type != null ? type.toLowerCase() : "info";
        this.title = title;
        this.message = message;
        this.msg = message;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.time = formatRelativeTime(createdAt);
    }

    private String formatRelativeTime(LocalDateTime dt) {
        if (dt == null) return "recently";
        long minutes = java.time.Duration.between(dt, LocalDateTime.now()).toMinutes();
        if (minutes < 1) return "just now";
        if (minutes < 60) return minutes + " min ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + " hr ago";
        long days = hours / 24;
        return days + " days ago";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; this.msg = message; }
    public String getMsg() { return msg != null ? msg : message; }
    public void setMsg(String msg) { this.msg = msg; this.message = msg; }
    public String getTime() { return time != null ? time : formatRelativeTime(createdAt); }
    public void setTime(String time) { this.time = time; }
    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
