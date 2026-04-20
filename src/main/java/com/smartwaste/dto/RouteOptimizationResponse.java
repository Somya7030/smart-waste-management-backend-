package com.smartwaste.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteOptimizationResponse {
    private List<RouteCluster> clusters;
    private int totalBins;
    private double totalDistance;
    private String estimatedTime;
    private String routeSummary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteCluster {
        private int clusterIndex;
        private String clusterLabel;
        private List<RouteBinStop> stops;
        private double clusterCenterLat;
        private double clusterCenterLon;
        private double clusterPriority;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteBinStop {
        private int stopOrder;
        private Long binId;
        private String binCode;
        private String location;
        private Double latitude;
        private Double longitude;
        private Double fillLevel;
        private String status;
        private Double priority;
        private String stopType; // "DEPOT", "BIN", "RETURN"
    }
}
