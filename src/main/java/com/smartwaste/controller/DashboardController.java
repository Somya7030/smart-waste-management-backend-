package com.smartwaste.controller;

import com.smartwaste.dto.ApiResponse;
import com.smartwaste.dto.DashboardStatsDTO;
import com.smartwaste.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * GET /api/stats
     * Returns aggregate dashboard statistics:
     * totalBins, fullBins, halfBins, emptyBins, activeAlerts, pendingReports,
     * averageFillLevel, collectionEfficiency
     */
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getStats() {
        return ResponseEntity.ok(
                ApiResponse.success("Dashboard statistics", dashboardService.getStats()));
    }
}
