package com.smartwaste.init;

import com.smartwaste.model.*;
import com.smartwaste.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds the database with realistic demo data on startup.
 * Covers bins spread across a city grid with varied fill levels.
 *
 * Active on ALL profiles (remove @Profile to restrict).
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final BinRepository         binRepository;
    private final AlertRepository       alertRepository;
    private final CitizenReportRepository reportRepository;

    @Override
    public void run(String... args) {
        if (binRepository.count() > 0) {
            log.info("Database already seeded. Skipping initialization.");
            return;
        }
        log.info("Seeding database with demo data...");
        seedBins();
        seedAlerts();
        seedReports();
        log.info("Database seeding complete. {} bins loaded.", binRepository.count());
    }

    // ─── Bin Data (20 bins across Bangalore city zones) ─────────────────

    private void seedBins() {
        List<Bin> bins = List.of(
            makeBin("BIN-001", "MG Road, Zone A",        12.9758, 77.6085,  92.0),
            makeBin("BIN-002", "Koramangala Blk 5",      12.9352, 77.6245,  85.5),
            makeBin("BIN-003", "Indiranagar 100ft Rd",   12.9784, 77.6408,  35.0),
            makeBin("BIN-004", "Whitefield Main Rd",     12.9698, 77.7499,  67.0),
            makeBin("BIN-005", "Jayanagar 4th Block",    12.9308, 77.5827,  88.0),
            makeBin("BIN-006", "Electronic City Ph1",    12.8399, 77.6770,  12.0),
            makeBin("BIN-007", "Marathahalli Bridge",    12.9591, 77.6974,  55.5),
            makeBin("BIN-008", "HSR Layout Sector 7",    12.9116, 77.6474,  91.0),
            makeBin("BIN-009", "Bannerghatta Rd",        12.8952, 77.5974,  23.0),
            makeBin("BIN-010", "Hebbal Flyover",         13.0358, 77.5969,  78.0),
            makeBin("BIN-011", "Rajajinagar West",       12.9897, 77.5538,  95.0),
            makeBin("BIN-012", "Yelahanka New Town",     13.1007, 77.5963,  41.0),
            makeBin("BIN-013", "BTM Layout 2nd Stage",   12.9166, 77.6101,  83.5),
            makeBin("BIN-014", "Vijayanagar Circle",     12.9720, 77.5344,  10.0),
            makeBin("BIN-015", "Shivajinagar Bus Stand", 12.9854, 77.6006,  62.0),
            makeBin("BIN-016", "Domlur Village Rd",      12.9609, 77.6387,  74.0),
            makeBin("BIN-017", "Sarjapur Outer Ring",    12.9072, 77.6861,  89.0),
            makeBin("BIN-018", "Nagarbhavi Circle",      12.9619, 77.5108,  5.0 ),
            makeBin("BIN-019", "JP Nagar Phase 6",       12.8991, 77.5908,  57.0),
            makeBin("BIN-020", "Basaveshwara Nagar",     13.0000, 77.5450,  81.0)
        );
        binRepository.saveAll(bins);
        log.info("Seeded {} bins.", bins.size());
    }

    private Bin makeBin(String code, String location, double lat, double lon, double fill) {
        BinStatus status;
        if (fill < 40.0)       status = BinStatus.EMPTY;
        else if (fill <= 80.0) status = BinStatus.HALF;
        else                   status = BinStatus.FULL;

        return Bin.builder()
                .binCode(code)
                .location(location)
                .latitude(lat)
                .longitude(lon)
                .fillLevel(fill)
                .status(status)
                .lastUpdatedTime(LocalDateTime.now().minusMinutes((long)(Math.random() * 120)))
                .build();
    }

    // ─── Alerts for FULL bins ────────────────────────────────────────────

    private void seedAlerts() {
        binRepository.findByStatus(BinStatus.FULL).forEach(bin -> {
            AlertSeverity severity = bin.getFillLevel() > 90.0
                    ? AlertSeverity.CRITICAL : AlertSeverity.WARNING;
            String msg = String.format("Bin %s at %s requires immediate collection (%.1f%% full).",
                    bin.getBinCode(), bin.getLocation(), bin.getFillLevel());
            Alert alert = Alert.builder()
                    .bin(bin)
                    .message(msg)
                    .severity(severity)
                    .fillLevelAtAlert(bin.getFillLevel())
                    .resolved(false)
                    .build();
            alertRepository.save(alert);
        });
        log.info("Seeded {} alerts.", alertRepository.count());
    }

    // ─── Citizen Reports ─────────────────────────────────────────────────

    private void seedReports() {
        List<CitizenReport> reports = List.of(
            CitizenReport.builder()
                .imagePath("demo_report_1.jpg")
                .location("MG Road near Metro Station")
                .description("Overflowing bin near metro entrance. Garbage spilling on sidewalk.")
                .latitude(12.9758).longitude(77.6085)
                .status(ReportStatus.PENDING)
                .reporterContact("9876543210")
                .timestamp(LocalDateTime.now().minusHours(2))
                .build(),
            CitizenReport.builder()
                .imagePath("demo_report_2.jpg")
                .location("Koramangala 5th Block Park")
                .description("Bin damaged and needs replacement. Also full.")
                .latitude(12.9352).longitude(77.6245)
                .status(ReportStatus.IN_REVIEW)
                .reporterContact("citizen@email.com")
                .timestamp(LocalDateTime.now().minusHours(5))
                .build(),
            CitizenReport.builder()
                .imagePath("demo_report_3.jpg")
                .location("Indiranagar 12th Main")
                .description("Garbage dumped illegally outside a closed shop.")
                .latitude(12.9784).longitude(77.6408)
                .status(ReportStatus.RESOLVED)
                .timestamp(LocalDateTime.now().minusDays(1))
                .build()
        );
        reportRepository.saveAll(reports);
        log.info("Seeded {} citizen reports.", reports.size());
    }
}
