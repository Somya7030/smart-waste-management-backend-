package com.smartwaste.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WasteClassificationDTO {
    private Long id;
    private String wasteType;
    private Double confidence;
    private String confidencePercent;    // e.g. "92%"
    private String imageUrl;
    private String additionalInfo;
    private LocalDateTime classifiedAt;
    private String disposalInstructions;
    private String recyclable;
}
