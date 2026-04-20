package com.smartwaste.controller;

import com.smartwaste.dto.ApiResponse;
import com.smartwaste.dto.RouteOptimizationRequest;
import com.smartwaste.dto.RouteOptimizationResponse;
import com.smartwaste.service.RouteOptimizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/route")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RouteOptimizationController {

    private final RouteOptimizationService routeOptimizationService;

    /**
     * POST /api/route/optimize
     *
     * ML-based route optimization using KMeans++ clustering and priority scoring.
     *
     * Request body (all fields optional):
     * {
     *   "depotLatitude": 12.9716,
     *   "depotLongitude": 77.5946,
     *   "clusterRadiusKm": 2.0,
     *   "fullBinsOnly": false,
     *   "maxBinsPerRoute": 20
     * }
     *
     * Response includes:
     * - Prioritized bins grouped by zone/cluster
     * - Total route distance
     * - Estimated collection time
     * - Route summary: Depot → Zone-A → Zone-B → Return
     */
    @PostMapping("/optimize")
    public ResponseEntity<ApiResponse<RouteOptimizationResponse>> optimizeRoute(
            @RequestBody(required = false) RouteOptimizationRequest request) {
        if (request == null) {
            request = new RouteOptimizationRequest();
        }
        log.info("Route optimization requested. Config: {}", request);
        RouteOptimizationResponse result = routeOptimizationService.optimizeRoute(request);
        return ResponseEntity.ok(
                ApiResponse.success("Route optimized successfully", result));
    }
}
