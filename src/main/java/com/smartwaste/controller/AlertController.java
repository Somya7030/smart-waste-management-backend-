package com.smartwaste.controller;

import com.smartwaste.dto.AlertDTO;
import com.smartwaste.dto.ApiResponse;
import com.smartwaste.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AlertController {

    private final AlertService alertService;

    /**
     * GET /api/alerts
     * Returns all active (unresolved) alerts — bins with fillLevel > 80%.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AlertDTO>>> getActiveAlerts() {
        return ResponseEntity.ok(
                ApiResponse.success("Active alerts retrieved", alertService.getActiveAlerts()));
    }

    /**
     * GET /api/alerts/all
     * Returns full alert history (resolved + active).
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<AlertDTO>>> getAllAlerts() {
        return ResponseEntity.ok(
                ApiResponse.success("All alerts retrieved", alertService.getAllAlerts()));
    }

    /**
     * PATCH /api/alerts/{id}/resolve
     * Manually marks an alert as resolved.
     */
    @PatchMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<AlertDTO>> resolveAlert(@PathVariable Long id) {
        AlertDTO resolved = alertService.resolveAlertById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Alert resolved successfully", resolved));
    }
}
