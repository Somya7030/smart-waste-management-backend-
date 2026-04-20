package com.smartwaste.dto;

import com.smartwaste.model.BinStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapBinDTO {
    private Long binId;
    private String binCode;
    private Double latitude;
    private Double longitude;
    private BinStatus status;
    private Double fillLevel;
    private String location;
}
