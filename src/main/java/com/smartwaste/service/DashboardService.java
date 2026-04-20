package com.smartwaste.service;

import com.smartwaste.dto.DashboardStatsDTO;
import com.smartwaste.model.BinStatus;
import com.smartwaste.model.ReportStatus;
import com.smartwaste.repository.BinRepository;
import com.smartwaste.repository.CitizenReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardService {

    private final BinRepository binRepository;
    private final CitizenReportRepository reportRepository;
    private final AlertService alertService;

    public DashboardStatsDTO getStats() {
        long totalBins  = binRepository.count();
        long fullBins   = binRepository.countByStatus(BinStatus.FULL);
        long halfBins   = binRepository.countByStatus(BinStatus.HALF);
        long emptyBins  = binRepository.countByStatus(BinStatus.EMPTY);
        long activeAlerts = alertService.countActiveAlerts();
        long pendingReports = reportRepository.countByStatus(ReportStatus.PENDING);

        Double avgFill = binRepository.averageFillLevel();
        double averageFillLevel = (avgFill != null) ? Math.round(avgFill * 10.0) / 10.0 : 0.0;

        double collectionEfficiency = totalBins > 0
                ? Math.round(((double)(totalBins - fullBins) / totalBins) * 1000.0) / 10.0
                : 100.0;

        log.debug("Dashboard stats computed: total={}, full={}, half={}, empty={}",
                totalBins, fullBins, halfBins, emptyBins);

        return DashboardStatsDTO.builder()
                .totalBins(totalBins)
                .fullBins(fullBins)
                .halfBins(halfBins)
                .emptyBins(emptyBins)
                .activeAlerts(activeAlerts)
                .pendingReports(pendingReports)
                .averageFillLevel(averageFillLevel)
                .collectionEfficiency(collectionEfficiency)
                .build();
    }
}
