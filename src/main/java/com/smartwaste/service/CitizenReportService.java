package com.smartwaste.service;

import com.smartwaste.dto.CitizenReportDTO;
import com.smartwaste.exception.FileStorageException;
import com.smartwaste.exception.ResourceNotFoundException;
import com.smartwaste.model.CitizenReport;
import com.smartwaste.model.ReportStatus;
import com.smartwaste.repository.CitizenReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CitizenReportService {

    private final CitizenReportRepository reportRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Transactional
    public CitizenReportDTO submitReport(MultipartFile image,
                                         String location,
                                         String description,
                                         Double latitude,
                                         Double longitude,
                                         String reporterContact) {
        log.info("New citizen report submitted for location: {}", location);

        String imagePath = storeFile(image);

        CitizenReport report = CitizenReport.builder()
                .imagePath(imagePath)
                .location(location)
                .description(description)
                .latitude(latitude)
                .longitude(longitude)
                .reporterContact(reporterContact)
                .status(ReportStatus.PENDING)
                .build();

        CitizenReport saved = reportRepository.save(report);
        log.info("Citizen report saved with id: {}", saved.getId());
        return toDTO(saved);
    }

    public List<CitizenReportDTO> getAllReports() {
        return reportRepository.findAllByOrderByTimestampDesc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CitizenReportDTO getReportById(Long id) {
        CitizenReport report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));
        return toDTO(report);
    }

    @Transactional
    public CitizenReportDTO updateReportStatus(Long id, ReportStatus status) {
        CitizenReport report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));
        report.setStatus(status);
        return toDTO(reportRepository.save(report));
    }

    // ─── File Storage ────────────────────────────────────────────────────

    private String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("File is empty or not provided");
        }

        String originalFileName = StringUtils.cleanPath(
                Objects.requireNonNull(file.getOriginalFilename()));

        // Sanitize and add UUID prefix to prevent collisions
        String extension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFileName.substring(dotIndex);
        }
        String storedFileName = UUID.randomUUID() + extension;

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            Path targetLocation = uploadPath.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.debug("File stored: {}", storedFileName);
            return storedFileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + originalFileName, ex);
        }
    }

    // ─── Mapper ──────────────────────────────────────────────────────────

    private CitizenReportDTO toDTO(CitizenReport report) {
        return CitizenReportDTO.builder()
                .id(report.getId())
                .imageUrl("/api/reports/image/" + report.getImagePath())
                .location(report.getLocation())
                .description(report.getDescription())
                .latitude(report.getLatitude())
                .longitude(report.getLongitude())
                .status(report.getStatus())
                .timestamp(report.getTimestamp())
                .reporterContact(report.getReporterContact())
                .build();
    }
}
