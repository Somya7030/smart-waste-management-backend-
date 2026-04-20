package com.smartwaste.repository;

import com.smartwaste.model.Alert;
import com.smartwaste.model.AlertSeverity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByResolvedFalseOrderByCreatedAtDesc();

    List<Alert> findBySeverityAndResolvedFalse(AlertSeverity severity);

    @Query("SELECT a FROM Alert a WHERE a.bin.id = :binId ORDER BY a.createdAt DESC")
    List<Alert> findByBinIdOrderByCreatedAtDesc(Long binId);

    @Query("SELECT COUNT(a) FROM Alert a WHERE a.resolved = false")
    long countActiveAlerts();

    @Query("SELECT a FROM Alert a WHERE a.bin.id = :binId AND a.resolved = false")
    List<Alert> findActiveAlertsByBinId(Long binId);
}
