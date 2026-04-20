package com.smartwaste.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BinUpdateRequest {

    @NotBlank(message = "Bin code is required")
    private String binCode;

    @NotNull(message = "Fill level is required")
    @DecimalMin(value = "0.0", message = "Fill level cannot be negative")
    @DecimalMax(value = "100.0", message = "Fill level cannot exceed 100")
    private Double fillLevel;

    private Double latitude;
    private Double longitude;
    private String location;
}
