package com.smartwaste.controller;

import com.smartwaste.dto.ApiResponse;
import com.smartwaste.dto.CitizenReportDTO;
import com.smartwaste.model.ReportStatus;
import com.smartwaste.service.CitizenReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CitizenReportController {

    private final CitizenReportService reportService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * POST /api/reports
     * Submit a new citizen waste report with an image.
     *
     * Form fields:
     *   image           (MultipartFile, required)
     *   location        (String, required)
     *   description     (String, required)
     *   latitude        (Double, optional)
     *   longitude       (Double, optional)
     *   reporterContact (String, optional)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CitizenReportDTO>> submitReport(
            @RequestParam("image") MultipartFile image,
            @RequestParam("location") String location,
            @RequestParam("description") String description,
            @RequestParam(value = "latitude",  required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "reporterContact", required = false) String reporterContact) {

        log.info("Citizen report received for location: {}", location);
        CitizenReportDTO dto = reportService.submitReport(
                image, location, description, latitude, longitude, reporterContact);
        return ResponseEntity.ok(
                ApiResponse.success("Report submitted successfully", dto));
    }

    /**
     * GET /api/reports
     * Returns all citizen reports, newest first.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CitizenReportDTO>>> getAllReports() {
        return ResponseEntity.ok(
                ApiResponse.success("Reports retrieved", reportService.getAllReports()));
    }

    /**
     * GET /api/reports/{id}
     * Returns a single report by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CitizenReportDTO>> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(reportService.getReportById(id)));
    }

    /**
     * PATCH /api/reports/{id}/status
     * Update report status (PENDING → IN_REVIEW → RESOLVED / REJECTED).
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CitizenReportDTO>> updateStatus(
            @PathVariable Long id,
            @RequestParam ReportStatus status) {
        CitizenReportDTO updated = reportService.updateReportStatus(id, status);
        return ResponseEntity.ok(
                ApiResponse.success("Status updated to " + status, updated));
    }

    /**
     * GET /api/reports/image/{fileName}
     * Serves the uploaded image file.
     */
    @GetMapping("/image/{fileName}")
    public ResponseEntity<Resource> serveImage(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            String contentType = determineContentType(fileName);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (MalformedURLException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    private String determineContentType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".gif"))  return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }
}
