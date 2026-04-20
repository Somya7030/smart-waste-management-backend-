package com.smartwaste.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteOptimizationRequest {
    private Double depotLatitude;
    private Double depotLongitude;
    private Double clusterRadiusKm;   // optional override
    private boolean fullBinsOnly;     // if true, only include FULL bins
    private Integer maxBinsPerRoute;  // optional cap
}
