package com.smartwaste.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private long totalBins;
    private long fullBins;
    private long halfBins;
    private long emptyBins;
    private long activeAlerts;
    private long pendingReports;
    private double averageFillLevel;
    private double collectionEfficiency;  // % of bins not FULL
}
