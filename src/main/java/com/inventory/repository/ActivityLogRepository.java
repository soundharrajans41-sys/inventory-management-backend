package com.inventory.repository;

import com.inventory.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findAllByOrderByTimestampDesc();
    List<ActivityLog> findTop20ByOrderByTimestampDesc();
    List<ActivityLog> findTop50ByOrderByTimestampDesc();
    List<ActivityLog> findByModuleOrderByTimestampDesc(String module);
}
