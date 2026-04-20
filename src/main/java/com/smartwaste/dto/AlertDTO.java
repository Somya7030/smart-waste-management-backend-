package com.smartwaste.dto;

import com.smartwaste.model.AlertSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertDTO {
    private Long id;
    private Long binId;
    private String binCode;
    private String binLocation;
    private String message;
    private AlertSeverity severity;
    private Double fillLevelAtAlert;
    private LocalDateTime createdAt;
    private boolean resolved;
    private Double latitude;
    private Double longitude;
}
