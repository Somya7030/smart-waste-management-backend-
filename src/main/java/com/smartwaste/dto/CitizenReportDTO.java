package com.smartwaste.dto;

import com.smartwaste.model.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitizenReportDTO {
    private Long id;
    private String imageUrl;
    private String location;
    private String description;
    private Double latitude;
    private Double longitude;
    private ReportStatus status;
    private LocalDateTime timestamp;
    private String reporterContact;
}
