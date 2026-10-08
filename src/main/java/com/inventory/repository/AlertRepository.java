package com.inventory.repository;

import com.inventory.entity.Alert;
import com.inventory.entity.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findAllByOrderByCreatedAtDesc();

    List<Alert> findByIsReadFalseOrderByCreatedAtDesc();

    long countByIsReadFalse();

    boolean existsByTypeAndReferenceIdAndIsReadFalse(AlertType type, String referenceId);

    void deleteByReferenceTypeAndReferenceId(String referenceType, String referenceId);
}
