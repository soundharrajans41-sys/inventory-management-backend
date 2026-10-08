package com.inventory.controller;

import com.inventory.dto.ActivityLogResponse;
import com.inventory.service.ActivityLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/activity-logs")
@Tag(name = "Activity Logs", description = "Endpoints for viewing admin and system audit logs")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all activity logs (Admin only)")
    public ResponseEntity<List<ActivityLogResponse>> getAllLogs() {
        return ResponseEntity.ok(activityLogService.getAllLogs());
    }

    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get 20 most recent activity logs (Admin only)")
    public ResponseEntity<List<ActivityLogResponse>> getRecentLogs() {
        return ResponseEntity.ok(activityLogService.getRecentLogs());
    }
}
