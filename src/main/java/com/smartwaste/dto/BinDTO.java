package com.smartwaste.dto;

import com.smartwaste.model.BinStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BinDTO {
    private Long id;
    private String binCode;
    private String location;
    private Double fillLevel;
    private BinStatus status;
    private Double latitude;
    private Double longitude;
    private LocalDateTime lastUpdatedTime;
}
