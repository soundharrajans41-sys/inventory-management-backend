package com.inventory.service;

import com.inventory.dto.ActivityLogResponse;
import com.inventory.entity.ActivityLog;
import com.inventory.entity.User;
import com.inventory.repository.ActivityLogRepository;
import com.inventory.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ActivityLogService {

    private static final Logger log = LoggerFactory.getLogger(ActivityLogService.class);

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository, UserRepository userRepository) {
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
    }

    public List<ActivityLogResponse> getAllLogs() {
        return activityLogRepository.findAllByOrderByTimestampDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ActivityLogResponse> getRecentLogs() {
        return activityLogRepository.findTop20ByOrderByTimestampDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void log(String action, String module, String description) {
        try {
            String userEmail = "system";
            String userName = "System";

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                userEmail = auth.getName();
                Optional<User> u = userRepository.findByEmail(userEmail);
                if (u.isPresent()) {
                    userName = u.get().getName();
                } else {
                    userName = userEmail;
                }
            }

            ActivityLog logEntity = new ActivityLog(userEmail, userName, action, module, description);
            activityLogRepository.save(logEntity);
        } catch (Exception e) {
            log.warn("Failed to record activity log: {}", e.getMessage());
        }
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void logDirect(String userEmail, String userName, String action, String module, String description) {
        try {
            ActivityLog logEntity = new ActivityLog(userEmail, userName, action, module, description);
            activityLogRepository.save(logEntity);
        } catch (Exception e) {
            log.warn("Failed to record activity log: {}", e.getMessage());
        }
    }

    public ActivityLogResponse mapToResponse(ActivityLog log) {
        return new ActivityLogResponse(
                log.getId(),
                log.getUserEmail(),
                log.getUserName(),
                log.getAction(),
                log.getModule(),
                log.getDescription(),
                log.getTimestamp()
        );
    }
}
