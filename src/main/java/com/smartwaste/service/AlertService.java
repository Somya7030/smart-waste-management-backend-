package com.smartwaste.service;

import com.smartwaste.dto.AlertDTO;
import com.smartwaste.exception.ResourceNotFoundException;
import com.smartwaste.model.Alert;
import com.smartwaste.model.AlertSeverity;
import com.smartwaste.model.Bin;
import com.smartwaste.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;

    /**
     * Creates a new alert only if no active alert already exists for this bin.
     */
    @Transactional
    public void createAlertIfNotExists(Bin bin) {
        List<Alert> existing = alertRepository.findActiveAlertsByBinId(bin.getId());
        if (!existing.isEmpty()) {
            log.debug("Active alert already exists for bin: {}", bin.getBinCode());
            return;
        }

        AlertSeverity severity = bin.getFillLevel() > 90.0 ? AlertSeverity.CRITICAL : AlertSeverity.WARNING;
        String message = String.format("Bin %s at %s is %s (%.1f%% full). Immediate collection required.",
                bin.getBinCode(), bin.getLocation(), severity.name(), bin.getFillLevel());

        Alert alert = Alert.builder()
                .bin(bin)
                .message(message)
                .severity(severity)
                .fillLevelAtAlert(bin.getFillLevel())
                .resolved(false)
                .build();

        alertRepository.save(alert);
        log.warn("ALERT created for bin: {} - {}", bin.getBinCode(), severity);
    }

    /**
     * Resolves all active alerts for a bin once it is emptied.
     */
    @Transactional
    public void resolveAlerts(Bin bin) {
        List<Alert> activeAlerts = alertRepository.findActiveAlertsByBinId(bin.getId());
        activeAlerts.forEach(alert -> {
            alert.setResolved(true);
            alert.setResolvedAt(LocalDateTime.now());
        });
        if (!activeAlerts.isEmpty()) {
            alertRepository.saveAll(activeAlerts);
            log.info("Resolved {} alert(s) for bin: {}", activeAlerts.size(), bin.getBinCode());
        }
    }

    public List<AlertDTO> getActiveAlerts() {
        return alertRepository.findByResolvedFalseOrderByCreatedAtDesc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<AlertDTO> getAllAlerts() {
        return alertRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AlertDTO resolveAlertById(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + alertId));
        alert.setResolved(true);
        alert.setResolvedAt(LocalDateTime.now());
        return toDTO(alertRepository.save(alert));
    }

    public long countActiveAlerts() {
        return alertRepository.countActiveAlerts();
    }

    private AlertDTO toDTO(Alert alert) {
        return AlertDTO.builder()
                .id(alert.getId())
                .binId(alert.getBin().getId())
                .binCode(alert.getBin().getBinCode())
                .binLocation(alert.getBin().getLocation())
                .message(alert.getMessage())
                .severity(alert.getSeverity())
                .fillLevelAtAlert(alert.getFillLevelAtAlert())
                .createdAt(alert.getCreatedAt())
                .resolved(alert.isResolved())
                .latitude(alert.getBin().getLatitude())
                .longitude(alert.getBin().getLongitude())
                .build();
    }
}
